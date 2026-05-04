package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.BlockchainDataService;
import pl.skompilowani.service.GasPriceService;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.util.HashShortener;

import java.util.List;

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

        logger.info("Sprawdzanie statusu sieci Sepolia...");
        if (!client.checkNetworkStatus()) {
            logger.error("Aplikacja kończy działanie z powodu braku połączenia z siecią.");
            return;
        }

        // Uruchomienie Serwisów
        BlockchainDataService dataService = new BlockchainDataService(client);
        GasPriceService gasPriceService = new GasPriceService(client);

        logger.info("--- ROZPOCZYNAM ZADANIE: Pobieranie Bloków i Transakcji ---");
        List<BlockDTO> blocks = dataService.fetchLatestBlocksData();

        // Warstwa Raportowania (Prezentacji)
        logger.info("--- RAPORT KOŃCOWY ---");
        for (BlockDTO block : blocks) {
            logger.info("Blok: {} | Hash: {} | Tx: {}", block.number(), block.hash(), block.transactionCount());

            if (block.transactions() != null && !block.transactions().isEmpty()) {
                for (TransactionDTO tx : block.transactions()) {

                    logger.info("  -> TX: {} | Od: {} | Do: {} | Warto : {} ETH | Zu ycie Gasu: {} | Data (Unix): {}",
                            HashShortener.shorten(tx.hash()),
                            HashShortener.shorten(tx.from()),
                            tx.to() != null ? HashShortener.shorten(tx.to()) : "Tworzenie Kontraktu",
                            tx.valueEth(),
                            tx.gasUsed(),
                            tx.timestamp());
                }
            }
        }

        try {
            gasPriceService.calculateAverageGasPriceFor100Blocks();
        } catch (Exception e) {
            logger.error("Błąd podczas sprawdzania ceny gazu: ", e);
        }

        logger.info("Zakończono działanie aplikacji.");
    }
}