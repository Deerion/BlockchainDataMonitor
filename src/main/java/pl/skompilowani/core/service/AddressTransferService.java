package pl.skompilowani.core.service;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.core.model.AddressMatchMode;
import pl.skompilowani.infrastructure.client.alchemy.AlchemyAssetTransferClient;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.shared.util.UnitConverter;
import pl.skompilowani.core.model.AddressTransferDTO;

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

    // Ustalamy stałą określającą głębokość skanowania wstecz (np. 50 000 bloków)
    private static final int SAFE_BLOCK_WINDOW = 50000;

    public AddressTransferService(String rpcUrl, BlockchainClient blockchainClient) {
        this.client = new AlchemyAssetTransferClient(rpcUrl);
        this.blockchainClient = blockchainClient;
    }

    public List<AddressTransferDTO> findLatest(String address, AddressMatchMode mode, int limit) throws Exception {
        // 1. Pobieramy numer najnowszego bloku z sieci
        BigInteger latestBlock = blockchainClient.getLatestBlockNumber();

        // 2. Wyliczamy blok startowy (najnowszy - 50 000)
        BigInteger fromBlockNum = latestBlock.subtract(BigInteger.valueOf(SAFE_BLOCK_WINDOW));
        if (fromBlockNum.compareTo(BigInteger.ZERO) < 0) {
            fromBlockNum = BigInteger.ZERO;
        }

        // 3. Konwertujemy na format Hex wymagany przez Alchemy (np. "0x17f1a2")
        String fromBlockHex = "0x" + fromBlockNum.toString(16);

        // 4. Przekazujemy bezpieczny zakres bloku do klienta danych
        List<AddressTransferDTO> transfers = switch (mode) {
            case FROM -> client.getTransfersByFromAddress(address, limit, fromBlockHex);
            case TO -> client.getTransfersByToAddress(address, limit, fromBlockHex);
            case FROM_OR_TO -> mergeAndDeduplicate(address, limit, fromBlockHex);
        };
        return enrichWithGasFees(transfers);
    }

    private List<AddressTransferDTO> mergeAndDeduplicate(String address, int limit, String fromBlockHex) throws Exception {
        List<AddressTransferDTO> fromList = client.getTransfersByFromAddress(address, limit, fromBlockHex);
        List<AddressTransferDTO> toList = client.getTransfersByToAddress(address, limit, fromBlockHex);

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