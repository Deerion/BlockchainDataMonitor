package pl.skompilowani.service.filter;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.api.AlchemyAssetTransferClient;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.UnitConverter;
import pl.skompilowani.service.dto.AddressTransferDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AddressTransferService {

    private final AlchemyAssetTransferClient client;
    private final BlockchainClient blockchainClient;

    public AddressTransferService(String rpcUrl, BlockchainClient blockchainClient) {
        this.client = new AlchemyAssetTransferClient(rpcUrl);
        this.blockchainClient = blockchainClient;
    }

    public List<AddressTransferDTO> findLatest(String address, AddressMatchMode mode, int limit) throws Exception {
        List<AddressTransferDTO> transfers = switch (mode) {
            case FROM -> client.getTransfersByFromAddress(address, limit);
            case TO -> client.getTransfersByToAddress(address, limit);
            case FROM_OR_TO -> mergeAndDeduplicate(address, limit);
        };
        return enrichWithGasFees(transfers);
    }

    private List<AddressTransferDTO> mergeAndDeduplicate(String address, int limit) throws Exception {
        List<AddressTransferDTO> fromList = client.getTransfersByFromAddress(address, limit);
        List<AddressTransferDTO> toList = client.getTransfersByToAddress(address, limit);

        Map<String, AddressTransferDTO> seen = new LinkedHashMap<>();
        for (AddressTransferDTO t : fromList) {
            if (t.hash() != null) seen.put(t.hash(), t);
        }
        for (AddressTransferDTO t : toList) {
            if (t.hash() != null) seen.putIfAbsent(t.hash(), t);
        }

        return seen.values().stream()
                .sorted(Comparator.comparing(AddressTransferDTO::blockNumber).reversed())
                .limit(limit)
                .toList();
    }

    // Makes one eth_getTransactionReceipt call per transfer to fetch gasUsed and effectiveGasPrice.
    // For a result set of 10 this adds 10 sequential RPC calls — acceptable latency for interactive use.
    private List<AddressTransferDTO> enrichWithGasFees(List<AddressTransferDTO> transfers) {
        List<AddressTransferDTO> result = new ArrayList<>(transfers.size());
        for (AddressTransferDTO t : transfers) {
            BigInteger gasUsed = BigInteger.ZERO;
            BigDecimal gasFeeEth = BigDecimal.ZERO;
            if (t.hash() != null) {
                try {
                    Optional<TransactionReceipt> opt = blockchainClient.getTransactionReceipt(t.hash());
                    if (opt.isPresent()) {
                        TransactionReceipt r = opt.get();
                        gasUsed = r.getGasUsed() != null ? r.getGasUsed() : BigInteger.ZERO;
                        String gasPriceHex = r.getEffectiveGasPrice();
                        // effectiveGasPrice is hex (e.g. "0xa1b2c3"); substring(2) strips "0x".
                        if (gasPriceHex != null && gasPriceHex.length() > 2) {
                            BigInteger gasPrice = new BigInteger(gasPriceHex.substring(2), 16);
                            gasFeeEth = UnitConverter.weiToEther(gasUsed.multiply(gasPrice));
                        }
                    }
                } catch (Exception ignored) {}
            }
            result.add(new AddressTransferDTO(
                    t.hash(), t.from(), t.to(), t.blockNumber(),
                    t.value(), t.asset(), t.category(), t.blockTimestamp(),
                    gasUsed, gasFeeEth
            ));
        }
        return result;
    }
}
