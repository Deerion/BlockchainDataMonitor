package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;

public class Main {
    // Inicjalizacja loggera dla klasy Main
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

        // Sprawdzenie statusu sieci za pomocą nowej metody
        logger.info("Sprawdzanie statusu sieci Sepolia...");
        if (!client.checkNetworkStatus()) {
            logger.error("Aplikacja kończy działanie z powodu braku połączenia z siecią.");
            return;
        }

        // Właściwe pobieranie danych (jeśli sieć odpowiada)
        try {
            logger.info("Rozpoczynanie pobierania danych...");
            BigInteger latestNum = client.getLatestBlockNumber();
            logger.info("Sukces! Najnowszy numer bloku: {}", latestNum);

            var block = client.getBlockDetails(latestNum);
            logger.info("Dane bloku -> Hash: {}, Liczba transakcji: {}",
                    block.getHash(), block.getTransactions().size());

        } catch (Exception e) {
            logger.error("Wystąpił błąd podczas komunikacji z blockchainem: ", e);
        }
    }
}