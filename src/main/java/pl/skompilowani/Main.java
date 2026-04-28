package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.GasPriceService;
import pl.skompilowani.service.dto.BlockDTO; // Dodany import
import pl.skompilowani.service.mapper.BlockchainMapper; // Dodany import
import java.math.BigDecimal;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import java.util.Optional;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Uruchamianie Monitora Danych Blockchain...");

        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("BLOCKCHAIN_URL");

        if (url == null || url.isEmpty()) {
            logger.error("BŁĄD: Nie znaleziono zmiennej BLOCKCHAIN_URL w pliku .env!");
            return;
        }

        BlockchainClient client = new BlockchainClient(url);
        GasPriceService gasPriceService = new GasPriceService(client);

        logger.info("Sprawdzanie statusu sieci Sepolia...");
        if (!client.checkNetworkStatus()) {
            logger.error("Aplikacja kończy działanie z powodu braku połączenia z siecią.");
            return;
        }

        // 2. Pobieranie danych (jeśli sieć odpowiada)
        try {
            logger.info("Rozpoczynanie pobierania danych...");
            BigInteger latestNum = client.getLatestBlockNumber();
            logger.info("Najnowszy numer bloku to: {}", latestNum);

            int blocksToFetch = 100;
            BigInteger startBlock = latestNum.subtract(BigInteger.valueOf(blocksToFetch - 1));

            // Zapobiega ujemnym wartościom bloków
            if (startBlock.compareTo(BigInteger.ZERO) < 0) {
                startBlock = BigInteger.ZERO;
            }

            logger.info("Rozpoczynam pobieranie dokładnie {} ostatnich bloków (od bloku {} do {})...",
                    blocksToFetch, startBlock, latestNum);

            // Pętla pobierająca bloki w wyliczonym zakresie
            for (BigInteger currentBlockNum = startBlock;
                 currentBlockNum.compareTo(latestNum) <= 0;
                 currentBlockNum = currentBlockNum.add(BigInteger.ONE)) {

                // Wywołanie metody pobierającej dane bloku
                var block = client.getBlockDetails(currentBlockNum);

                logger.info("Pobrano blok nr {} | Hash: {} | Liczba transakcji: {}",
                        currentBlockNum, block.getHash(), block.getTransactions().size());

                // Sprawdzenie czy blok należy do 10 ostatnich
                BigInteger subsetStart = latestNum.subtract(BigInteger.valueOf(9));

                if (currentBlockNum.compareTo(subsetStart) >= 0 && !block.getTransactions().isEmpty()) {
                    logger.info("  Analizowanie transakcji dla bloku {}...", currentBlockNum);

                    // Limit do 5 transakcji na blok
                    int txLimit = Math.min(block.getTransactions().size(), 5);

                    for (int i = 0; i < txLimit; i++) {
                        TransactionObject tx = (TransactionObject) block.getTransactions().get(i).get();

                        String txHash = tx.getHash();
                        String from = tx.getFrom();
                        String to = tx.getTo(); // Może być null w przypadku tworzenia kontraktu (Smart Contract Creation)

                        // Konwersja Wartości z Wei na ETH
                        BigDecimal valueInWei = new BigDecimal(tx.getValue());
                        BigDecimal valueInEth = Convert.fromWei(valueInWei, Convert.Unit.ETHER);

                        // Pobieranie zużycia Gasu
                        Optional<TransactionReceipt> receiptOpt = client.getTransactionReceipt(txHash);
                        BigInteger gasUsed = BigInteger.ZERO;
                        if (receiptOpt.isPresent()) {
                            gasUsed = receiptOpt.get().getGasUsed();
                        }

                        logger.info("    -> TX: {} | Od: {} | Do: {} | Wartość: {} ETH | Zużycie Gasu: {}",
                                txHash, from, to != null ? to : "Tworzenie Kontraktu", valueInEth, gasUsed);

                        Thread.sleep(100);
                    }
                }
                Thread.sleep(200);
            }

            logger.info("Zakończono sukcesem pobieranie {} najnowszych bloków.", blocksToFetch);

        }catch (InterruptedException e) {
            // Bezpieczna obsługa przerwania wątku
            logger.error("Wątek aplikacji został niespodziewanie przerwany: ", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Wystąpił błąd podczas komunikacji z blockchainem: ", e);
        }
    }
}