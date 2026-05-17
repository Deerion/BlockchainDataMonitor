package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.core.service.*;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.infrastructure.persistence.CsvLogger;
import pl.skompilowani.presentation.console.BlockchainConsoleUI;
import pl.skompilowani.shared.ui.ProgressBar;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String url = dotenv.get("BLOCKCHAIN_URL");

        if (url == null || url.isEmpty()) {
            logger.error("BŁĄD: Brak zmiennej BLOCKCHAIN_URL!");
            logger.error("Upewnij się, że plik .env istnieje w folderze głównym projektu");
            logger.error("lub zmienna środowiskowa jest poprawnie ustawiona w systemie.");
            return;
        }

        BlockchainClient client = new BlockchainClient(url);
        if (!client.checkNetworkStatus()) {
            return;
        }

        // Implementacja słuchacza postępu dedykowana dla konsoli (UI)
        ProgressListener consoleProgressListener = new ProgressListener() {
            @Override
            public void onProgress(int current, int total, String message) {
                ProgressBar.show(current, total, message);
            }

            @Override
            public void onProgressComplete() {
                // Koniec ładowania paska postępu
            }
        };

        // Tworzymy instancję repozytorium loggera z warstwy infrastruktury
        TransactionLogRepository csvLogger = new CsvLogger();

        SessionStatisticsService statsService = new SessionStatisticsService();

        // Wstrzykujemy zależności przez konstruktor (Dependency Injection)
        BlockchainDataService dataService = new BlockchainDataService(client, statsService, csvLogger, consoleProgressListener);
        GasPriceService gasPriceService = new GasPriceService(client, consoleProgressListener);

        AddressTransferService addressSvc = new AddressTransferService(url, client);
        ValueTransferService valueSvc = new ValueTransferService(client);

        BlockchainConsoleUI ui = new BlockchainConsoleUI(dataService, gasPriceService, addressSvc, valueSvc, statsService);
        ui.start();
    }
}