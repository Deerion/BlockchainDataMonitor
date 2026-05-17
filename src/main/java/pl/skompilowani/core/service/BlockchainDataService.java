package pl.skompilowani.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.core.model.TransactionDTO;
import pl.skompilowani.infrastructure.mapper.BlockchainMapper;
import pl.skompilowani.shared.util.UnitConverter;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BlockchainDataService {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainDataService.class);
    private final BlockchainClient client;
    private final SessionStatisticsService statsService;
    private final TransactionLogRepository transactionLogRepository;
    private final ProgressListener progressListener;

    public BlockchainDataService(BlockchainClient client, SessionStatisticsService statsService,
                                 TransactionLogRepository transactionLogRepository, ProgressListener progressListener) {
        this.client = client;
        this.statsService = statsService;
        this.transactionLogRepository = transactionLogRepository;
        this.progressListener = progressListener;
    }

    public List<BlockDTO> fetchLatestBlocksData() {
        List<BlockDTO> processedBlocks = new ArrayList<>();
        try {
            BigInteger latestNum = client.getLatestBlockNumber();
            int blocksToFetch = 100;
            BigInteger startBlock = latestNum.subtract(BigInteger.valueOf(blocksToFetch - 1));
            if (startBlock.compareTo(BigInteger.ZERO) < 0) startBlock = BigInteger.ZERO;

            BigInteger subsetStart = latestNum.subtract(BigInteger.valueOf(9));

            for (BigInteger current = startBlock; current.compareTo(latestNum) <= 0; current = current.add(BigInteger.ONE)) {
                progressListener.onProgress(processedBlocks.size() + 1, blocksToFetch, "Pobieranie bloków historycznych...");

                boolean fetchTxs = current.compareTo(subsetStart) >= 0;
                BlockDTO dto = processSingleBlock(current, fetchTxs);

                if (dto != null) {
                    processedBlocks.add(dto);
                    statsService.recordBlock(dto.transactionCount());
                }
                Thread.sleep(150);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania danych historycznych: ", e);
        }
        progressListener.onProgressComplete();
        return processedBlocks;
    }

    public void monitorRealTime(LiveMonitorListener listener) {
        try {
            BigInteger lastSeenBlock = client.getLatestBlockNumber();
            listener.onMonitorStart();

            while (true) {
                if (listener.shouldStop()) {
                    listener.onMonitorStopped();
                    break;
                }

                BigInteger currentLatest = client.getLatestBlockNumber();
                if (currentLatest.compareTo(lastSeenBlock) > 0) {
                    for (BigInteger b = lastSeenBlock.add(BigInteger.ONE); b.compareTo(currentLatest) <= 0; b = b.add(BigInteger.ONE)) {
                        BlockDTO dto = processSingleBlock(b, true);
                        if (dto != null) {
                            statsService.recordBlock(dto.transactionCount());

                            for (TransactionDTO tx : dto.transactions()) {
                                transactionLogRepository.logTransaction(dto, tx);
                            }

                            listener.onNewBlockProcessed(dto);
                        }
                    }
                    lastSeenBlock = currentLatest;
                }
                for (int i = 0; i < 50; i++) {
                    if (listener.shouldStop()) {
                        break;
                    }
                    Thread.sleep(100);
                }
            }
        } catch (Exception e) {
            logger.error("Błąd krytyczny monitoringu Real-time: ", e);
        }
    }

    private BlockDTO processSingleBlock(BigInteger blockNum, boolean fullDetails) throws Exception {
        EthBlock.Block raw = client.getBlockDetails(blockNum);
        if (raw == null) return null;

        List<TransactionDTO> txs = new ArrayList<>();
        if (fullDetails && raw.getTransactions() != null) {
            int limit = Math.min(raw.getTransactions().size(), 5);
            for (int i = 0; i < limit; i++) {
                TransactionObject tx = (TransactionObject) raw.getTransactions().get(i).get();

                var receiptOpt = client.getTransactionReceipt(tx.getHash());
                long gasUsed = 0;
                BigDecimal oplataEth = BigDecimal.ZERO;

                if (receiptOpt.isPresent()) {
                    var receipt = receiptOpt.get();
                    gasUsed = receipt.getGasUsed().longValue();

                    if (receipt.getEffectiveGasPrice() != null) {
                        BigInteger gasPrice = new BigInteger(receipt.getEffectiveGasPrice().substring(2), 16);
                        oplataEth = UnitConverter.weiToEther(BigInteger.valueOf(gasUsed).multiply(gasPrice));
                    }
                }

                BigDecimal valueEth = UnitConverter.weiToEther(tx.getValue());
                statsService.recordTransactionValue(valueEth, tx.getHash());

                txs.add(BlockchainMapper.toTransactionDTO(tx, gasUsed, raw.getTimestamp(), oplataEth));
            }
        }
        return BlockchainMapper.toBlockDTO(raw, txs);
    }
}