package pl.skompilowani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.service.mapper.BlockchainMapper;

import java.math.BigInteger;
import java.math.BigDecimal;
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
            int currentIndex = 0; // Dodany licznik do paska postępu

            for (BigInteger currentBlockNum = startBlock; currentBlockNum.compareTo(latestNum) <= 0; currentBlockNum = currentBlockNum.add(BigInteger.ONE)) {
                currentIndex++;
                pl.skompilowani.util.ProgressBar.show(currentIndex, blocksToFetch, "Pobieranie i analizowanie bloków z sieci...");

                var block = client.getBlockDetails(currentBlockNum);
                List<TransactionDTO> transactionDTOs = new ArrayList<>();

                // Mechanizm pobierania szczegółów transakcji
                if (currentBlockNum.compareTo(subsetStart) >= 0 && !block.getTransactions().isEmpty()) {
                    int txLimit = Math.min(block.getTransactions().size(), 5);
                    for (int i = 0; i < txLimit; i++) {
                        TransactionObject tx = (TransactionObject) block.getTransactions().get(i).get();
                        var receiptOpt = client.getTransactionReceipt(tx.getHash());
                        long gasUsed = receiptOpt.isPresent() ? receiptOpt.get().getGasUsed().longValue() : 0L;

                        // Próba pobrania efektywnej ceny gasu (fallbacky jeśli brak)
                        java.math.BigInteger effectiveGasPrice = null;
                        if (receiptOpt.isPresent()) {
                            try {
                                Object egpObj = receiptOpt.get().getEffectiveGasPrice();
                                if (egpObj instanceof java.math.BigInteger) {
                                    effectiveGasPrice = (java.math.BigInteger) egpObj;
                                } else if (egpObj != null) {
                                    String s = egpObj.toString();
                                    if (s.startsWith("0x") || s.startsWith("0X")) {
                                        effectiveGasPrice = org.web3j.utils.Numeric.decodeQuantity(s);
                                    } else {
                                        effectiveGasPrice = new java.math.BigInteger(s);
                                    }
                                }
                            } catch (Exception ignored) {
                                effectiveGasPrice = null;
                            }
                        }
                        if (effectiveGasPrice == null) {
                            try {
                                Object gpObj = tx.getGasPrice();
                                if (gpObj instanceof java.math.BigInteger) {
                                    effectiveGasPrice = (java.math.BigInteger) gpObj;
                                } else if (gpObj != null) {
                                    String s = gpObj.toString();
                                    if (s.startsWith("0x") || s.startsWith("0X")) {
                                        effectiveGasPrice = org.web3j.utils.Numeric.decodeQuantity(s);
                                    } else {
                                        effectiveGasPrice = new java.math.BigInteger(s);
                                    }
                                }
                            } catch (Exception ignored) {
                                effectiveGasPrice = null;
                            }
                        }
                        if (effectiveGasPrice == null) {
                            try {
                                Object baseFee = block.getBaseFeePerGas();
                                if (baseFee instanceof java.math.BigInteger) {
                                    effectiveGasPrice = (java.math.BigInteger) baseFee;
                                } else if (baseFee != null) {
                                    String s = baseFee.toString();
                                    if (s.startsWith("0x") || s.startsWith("0X")) {
                                        effectiveGasPrice = org.web3j.utils.Numeric.decodeQuantity(s);
                                    } else {
                                        effectiveGasPrice = new java.math.BigInteger(s);
                                    }
                                }
                            } catch (Exception ignored) {
                                effectiveGasPrice = null;
                            }
                        }

                        java.math.BigInteger feeWei = (effectiveGasPrice != null) ? BigInteger.valueOf(gasUsed).multiply(effectiveGasPrice) : BigInteger.ZERO;
                        BigDecimal feeEth = UnitConverter.weiToEther(feeWei);

                        // Wykorzystanie mappera z przekazanym kosztem (oplata)
                        transactionDTOs.add(BlockchainMapper.toTransactionDTO(tx, gasUsed, block.getTimestamp(), feeEth));
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

        System.out.println(); // Złamanie linii po zakończeniu paska

        return processedBlocks;
    }
}