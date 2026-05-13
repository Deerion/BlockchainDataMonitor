package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.*;
import pl.skompilowani.service.filter.*;
import pl.skompilowani.ui.BlockchainConsoleUI;

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

        SessionStatisticsService statsService = new SessionStatisticsService();
        BlockchainDataService dataService = new BlockchainDataService(client, statsService);
        GasPriceService gasPriceService = new GasPriceService(client);
        AddressTransferService addressSvc = new AddressTransferService(url, client);
        ValueTransferService valueSvc = new ValueTransferService(client);
        
        BlockchainConsoleUI ui = new BlockchainConsoleUI(dataService, gasPriceService, addressSvc, valueSvc, statsService);
        ui.start();
    }
}