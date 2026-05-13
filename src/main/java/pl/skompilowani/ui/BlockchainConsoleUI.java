package pl.skompilowani.ui;

import pl.skompilowani.service.*;
import pl.skompilowani.service.filter.AddressTransferService;
import pl.skompilowani.service.filter.ValueTransferService;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class BlockchainConsoleUI {
    private final SessionStatisticsService statsService;
    private final Scanner scanner;
    private final ConsoleInputValidator validator;
    private final TerminalDisplay display;
    private final UICommandHandler handler;
    private final Map<Integer, MenuOption> menuOptions = new LinkedHashMap<>();

    public BlockchainConsoleUI(BlockchainDataService dataSvc, GasPriceService gasSvc,
                               AddressTransferService addrSvc, ValueTransferService valSvc,
                               SessionStatisticsService statsService) {
        this.statsService = statsService;
        this.scanner = new Scanner(System.in);
        this.validator = new ConsoleInputValidator(scanner);
        this.display = new TerminalDisplay();
        this.handler = new UICommandHandler(dataSvc, gasSvc, addrSvc, valSvc, statsService, display, scanner, validator);

        initializeMenu();
    }

    private void initializeMenu() {
        // Każda opcja to polecenie (Command Pattern) zarejestrowane w mapie
        menuOptions.put(1, new MenuOption("Raport bloków (Konsola - 100 bloków)", handler::handleBlockReport));
        menuOptions.put(2, new MenuOption("Uruchom stały monitoring (Real-time + Logger CSV)", handler::handleRealTimeMonitor));
        menuOptions.put(3, new MenuOption("Oblicz średnią cenę Gas", handler::handleGasPriceCalculation));
        menuOptions.put(4, new MenuOption("Generuj pełny raport (.txt)", handler::handleReportGenerationToFile));
        menuOptions.put(5, new MenuOption("Filtrowanie transakcji (Podmenu KONSOLA/TXT)", handler::handleFilterSubmenu));
        menuOptions.put(6, new MenuOption("Wyjście i Podsumowanie sesji", handler::handleExit));
    }

    public void start() {
        while (true) {
            renderScreen();
            int choice = validator.getValidInt("Wybierz opcję: ", 1, menuOptions.size());
            display.clearConsole(); // Czyszczenie dla "Dashboard UX"
            menuOptions.get(choice).action().run();
        }
    }

    private void renderScreen() {
        display.clearConsole();
        display.printLogo();
        display.renderHeader(statsService);
        menuOptions.forEach((key, opt) -> System.out.println(key + ". " + opt.label()));
    }

    private record MenuOption(String label, Runnable action) {}
}