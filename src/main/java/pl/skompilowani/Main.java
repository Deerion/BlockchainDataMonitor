package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.BlockchainDataService;
import pl.skompilowani.service.GasPriceService;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.ui.ConsoleInputValidator;
import pl.skompilowani.util.DateFormatter;
import pl.skompilowani.util.HashShortener;
import pl.skompilowani.util.TableFormatter;
import pl.skompilowani.util.TerminalColorizer;

import java.util.List;
import java.util.Scanner;

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

        BlockchainDataService dataService = new BlockchainDataService(client);
        GasPriceService gasPriceService = new GasPriceService(client);

        runApplicationMenu(dataService, gasPriceService);

        logger.info("Zakończono działanie aplikacji.");
    }

    private static void runApplicationMenu(BlockchainDataService dataService, GasPriceService gasPriceService) {
        Scanner scanner = new Scanner(System.in);
        ConsoleInputValidator validator = new ConsoleInputValidator(scanner);
        boolean isRunning = true;

        while (isRunning) {
            printMenu();
            int choice = validator.getValidInt("Wybierz opcję (1-3): ", 1, 3);

            switch (choice) {
                case 1 -> handleBlockReport(dataService);
                case 2 -> handleGasPriceCalculation(gasPriceService);
                case 3 -> {
                    System.out.println("Zamykanie aplikacji. Do widzenia!");
                    isRunning = false;
                }
            }
        }
        scanner.close();
    }

    private static void printMenu() {
        System.out.println(TerminalColorizer.cyan("\n========================================"));
        System.out.println(TerminalColorizer.cyan("   MONITOR DANYCH BLOCKCHAIN (SEPOLIA)"));
        System.out.println(TerminalColorizer.cyan("========================================"));
        System.out.println("1. Wyświetl raport z ostatnich bloków i transakcji");
        System.out.println("2. Oblicz średnią cenę Gas (dla 100 bloków)");
        System.out.println("3. Wyjście");
        System.out.println(TerminalColorizer.cyan("========================================"));
    }

    private static void handleBlockReport(BlockchainDataService dataService) {
        logger.info(TerminalColorizer.yellow("--- ROZPOCZYNAM ZADANIE: Pobieranie Bloków i Transakcji ---"));
        List<BlockDTO> blocks = dataService.fetchLatestBlocksData();

        logger.info(TerminalColorizer.green("--- RAPORT KOŃCOWY ---"));
        for (BlockDTO block : blocks) {
            System.out.println(TerminalColorizer.cyan("\n###############################################################################################"));
            System.out.println(TerminalColorizer.green(String.format("BLOK: %d | Hash: %s | Ilość Tx: %d",
                    block.number(), HashShortener.shorten(block.hash()), block.transactionCount())));
            System.out.println(TerminalColorizer.cyan("###############################################################################################"));

            TableFormatter.printTransactionsTable(block.transactions());
        }
    }

    private static void handleGasPriceCalculation(GasPriceService gasPriceService) {
        try {
            gasPriceService.calculateAverageGasPriceFor100Blocks();
        } catch (Exception e) {
            logger.error("Błąd podczas sprawdzania ceny gazu: ", e);
        }
    }
}