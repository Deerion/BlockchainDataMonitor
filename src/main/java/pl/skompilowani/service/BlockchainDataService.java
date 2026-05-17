package pl.skompilowani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.service.mapper.BlockchainMapper;
import pl.skompilowani.util.*;

import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Serwis odpowiedzialny za logikę biznesową przetwarzania danych z blockchaina.
 * Realizuje wymogi monitorowania, gromadzenia statystyk oraz analizy transakcji.
 */
public class BlockchainDataService {
    private static final Logger logger = LoggerFactory.getLogger(BlockchainDataService.class);
    private final BlockchainClient client;
    private final SessionStatisticsService statsService;

    public BlockchainDataService(BlockchainClient client, SessionStatisticsService statsService) {
        this.client = client;
        this.statsService = statsService;
    }

    /**
     * Pobiera dane dla 100 najnowszych bloków (Wymóg MVP).
     * Dla 10 ostatnich bloków pobierane są szczegółowe dane transakcji.
     */
    public List<BlockDTO> fetchLatestBlocksData() {
        List<BlockDTO> processedBlocks = new ArrayList<>();
        try {
            BigInteger latestNum = client.getLatestBlockNumber();
            int blocksToFetch = 100;
            BigInteger startBlock = latestNum.subtract(BigInteger.valueOf(blocksToFetch - 1));
            if (startBlock.compareTo(BigInteger.ZERO) < 0) startBlock = BigInteger.ZERO;

            // Wyznaczenie progu dla szczegółowych danych (10 ostatnich)
            BigInteger subsetStart = latestNum.subtract(BigInteger.valueOf(9));

            for (BigInteger current = startBlock; current.compareTo(latestNum) <= 0; current = current.add(BigInteger.ONE)) {
                ProgressBar.show(processedBlocks.size() + 1, blocksToFetch, "Pobieranie bloków historycznych...");

                boolean fetchTxs = current.compareTo(subsetStart) >= 0;
                BlockDTO dto = processSingleBlock(current, fetchTxs);

                if (dto != null) {
                    processedBlocks.add(dto);
                    statsService.recordBlock(dto.transactionCount());
                }
                // Rate limiting zabezpieczający przed limitami API
                Thread.sleep(150);
            }
        } catch (Exception e) {
            logger.error("Błąd podczas pobierania danych historycznych: ", e);
        }
        System.out.println();
        return processedBlocks;
    }

    /**
     * Uruchamia monitoring sieci w czasie rzeczywistym.
     * Wyświetla informację o każdym nowym bloku i aktualizuje statystyki sesji.
     */
    /**
     * Uruchamia monitoring sieci w czasie rzeczywistym.
     * Zapisuje każdą transakcję do CSV wraz z kontekstem bloku.
     */
    public void monitorRealTime() {
        try {
            BigInteger lastSeenBlock = client.getLatestBlockNumber();
            System.out.println(TerminalColorizer.cyan(">>> Monitoring Live rozpoczęty."));
            System.out.println(TerminalColorizer.green(">>> Dane są automatycznie archiwizowane w pliku: live_stream.csv"));
            System.out.println(TerminalColorizer.yellow(">>> NACIŚNIJ [ENTER], ABY ZATRZYMAĆ I WRÓCIĆ DO MENU."));

            while (true) {
                if (System.in.available() > 0) {
                    System.in.read();
                    logger.info("Monitoring zatrzymany przez użytkownika.");
                    break;
                }

                BigInteger currentLatest = client.getLatestBlockNumber();
                if (currentLatest.compareTo(lastSeenBlock) > 0) {
                    for (BigInteger b = lastSeenBlock.add(BigInteger.ONE); b.compareTo(currentLatest) <= 0; b = b.add(BigInteger.ONE)) {
                        BlockDTO dto = processSingleBlock(b, true);
                        if (dto != null) {
                            statsService.recordBlock(dto.transactionCount());

                            // POPRAWKA: Przekazujemy blok 'dto' oraz transakcję 'tx'
                            for (TransactionDTO tx : dto.transactions()) {
                                CsvLogger.logTransaction(dto, tx);
                            }

                            // Wyświetlanie w konsoli (bez zmian)
                            System.out.println(TerminalColorizer.cyan("\n" + "#".repeat(FormatConstants.TABLE_WIDTH)));
                            System.out.println(TerminalColorizer.green(String.format("[%tT] NOWY BLOK #%d | Hash: %s | Transakcji: %d",
                                    new java.util.Date(), dto.number(), HashShortener.shorten(dto.hash()), dto.transactionCount())));
                            System.out.println(TerminalColorizer.cyan("#".repeat(FormatConstants.TABLE_WIDTH)));
                            TableFormatter.printTransactionsTable(dto.transactions());
                        }
                    }
                    lastSeenBlock = currentLatest;
                }
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            logger.error("Błąd krytyczny monitoringu Real-time: ", e);
        }
    }

    /**
     * Przetwarza pojedynczy blok, opcjonalnie pobierając detale transakcji i ich paragony.
     */
    private BlockDTO processSingleBlock(BigInteger blockNum, boolean fullDetails) throws Exception {
        EthBlock.Block raw = client.getBlockDetails(blockNum);
        if (raw == null) return null;

        List<TransactionDTO> txs = new ArrayList<>();
        if (fullDetails && raw.getTransactions() != null) {
            // Analizujemy podzbiór transakcji, aby nie przekroczyć limitów API
            int limit = Math.min(raw.getTransactions().size(), 5);
            for (int i = 0; i < limit; i++) {
                TransactionObject tx = (TransactionObject) raw.getTransactions().get(i).get();

                // Pobranie paragonu dla uzyskania faktycznego zużycia gazu i ceny
                var receiptOpt = client.getTransactionReceipt(tx.getHash());
                long gasUsed = 0;
                BigDecimal oplataEth = BigDecimal.ZERO;

                if (receiptOpt.isPresent()) {
                    var receipt = receiptOpt.get();
                    gasUsed = receipt.getGasUsed().longValue();

                    // Obliczanie faktycznej opłaty: GasUsed * EffectiveGasPrice
                    if (receipt.getEffectiveGasPrice() != null) {
                        BigInteger gasPrice = new BigInteger(receipt.getEffectiveGasPrice().substring(2), 16);
                        oplataEth = UnitConverter.weiToEther(BigInteger.valueOf(gasUsed).multiply(gasPrice));
                    }
                }

                BigDecimal valueEth = UnitConverter.weiToEther(tx.getValue());
                statsService.recordTransactionValue(valueEth, tx.getHash());

                // Mapowanie na DTO z uwzględnieniem obliczonej opłaty
                txs.add(BlockchainMapper.toTransactionDTO(tx, gasUsed, raw.getTimestamp(), oplataEth));
            }
        }
        return BlockchainMapper.toBlockDTO(raw, txs);
    }
}