package pl.skompilowani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.service.mapper.BlockchainMapper;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BlockchainDataService {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainDataService.class);
    private final BlockchainClient client;

    public BlockchainDataService(BlockchainClient client) {
        this.client = client;
    }

    public List<BlockDTO> fetchLatestBlocksData() {
        List<BlockDTO> processedBlocks = new ArrayList<>();
        try {
            BigInteger latestNum = client.getLatestBlockNumber();

            // Wyliczanie 100 bloków
            int blocksToFetch = 100;
            BigInteger startBlock = latestNum.subtract(BigInteger.valueOf(blocksToFetch - 1));

            if (startBlock.compareTo(BigInteger.ZERO) < 0) {
                startBlock = BigInteger.ZERO;
            }

            logger.info("Rozpoczynam pobieranie i mapowanie dokładnie {} ostatnich bloków (od bloku {} do {})...",
                    blocksToFetch, startBlock, latestNum);

            BigInteger subsetStart = latestNum.subtract(BigInteger.valueOf(9));

            for (BigInteger currentBlockNum = startBlock; currentBlockNum.compareTo(latestNum) <= 0; currentBlockNum = currentBlockNum.add(BigInteger.ONE)) {

                var block = client.getBlockDetails(currentBlockNum);
                List<TransactionDTO> transactionDTOs = new ArrayList<>();

                // Mechanizm pobierania szczegółów transakcji
                if (currentBlockNum.compareTo(subsetStart) >= 0 && !block.getTransactions().isEmpty()) {
                    int txLimit = Math.min(block.getTransactions().size(), 5);

                    for (int i = 0; i < txLimit; i++) {
                        TransactionObject tx = (TransactionObject) block.getTransactions().get(i).get();

                        var receiptOpt = client.getTransactionReceipt(tx.getHash());
                        long gasUsed = receiptOpt.isPresent() ? receiptOpt.get().getGasUsed().longValue() : 0L;

                        // Wykorzystanie mappera
                        transactionDTOs.add(BlockchainMapper.toTransactionDTO(tx, gasUsed, block.getTimestamp()));

                        Thread.sleep(100);
                    }
                }

                processedBlocks.add(BlockchainMapper.toBlockDTO(block, transactionDTOs));

                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            logger.error("Wątek przerwany: ", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania danych z blockchaina: ", e);
        }

        return processedBlocks;
    }
}