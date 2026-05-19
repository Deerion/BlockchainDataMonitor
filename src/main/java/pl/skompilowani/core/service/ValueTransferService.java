package pl.skompilowani.core.service;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.shared.util.UnitConverter;
import pl.skompilowani.core.model.AddressTransferDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ValueTransferService {

    private static final int BLOCKS_TO_SCAN = 20;

    private final BlockchainClient blockchainClient;

    public ValueTransferService(BlockchainClient blockchainClient) {
        this.blockchainClient = blockchainClient;
    }

    public List<AddressTransferDTO> findLatestAbove(BigDecimal minValueEth, int limit) throws Exception {
        BigInteger latestBlock = blockchainClient.getLatestBlockNumber();
        List<AddressTransferDTO> candidates = new ArrayList<>();

        for (int i = 0; i < BLOCKS_TO_SCAN && candidates.size() < limit * 5; i++) {
            BigInteger blockNum = latestBlock.subtract(BigInteger.valueOf(i));
            EthBlock.Block block = blockchainClient.getBlockDetails(blockNum);
            if (block == null || block.getTransactions() == null) continue;

            String timestamp = Instant.ofEpochSecond(block.getTimestamp().longValue()).toString();

            for (EthBlock.TransactionResult<?> txResult : block.getTransactions()) {
                if (!(txResult.get() instanceof TransactionObject tx)) continue;
                BigDecimal valueEth = UnitConverter.weiToEther(tx.getValue());
                if (valueEth.compareTo(minValueEth) >= 0) {
                    candidates.add(new AddressTransferDTO(
                            tx.getHash(), tx.getFrom(), tx.getTo(),
                            block.getNumber(), valueEth,
                            "ETH", "external", timestamp,
                            BigInteger.ZERO, BigDecimal.ZERO
                    ));
                }
            }
        }

        List<AddressTransferDTO> top = candidates.stream()
                .sorted(Comparator.comparing(AddressTransferDTO::blockNumber).reversed())
                .limit(limit)
                .toList();

        return enrichWithGasFees(top);
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
