package pl.skompilowani.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.service.*;
import pl.skompilowani.service.dto.AddressTransferDTO;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.filter.AddressMatchMode;
import pl.skompilowani.service.filter.AddressTransferService;
import pl.skompilowani.service.filter.ValueTransferService;
import pl.skompilowani.util.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class UICommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(UICommandHandler.class);

    private final BlockchainDataService dataService;
    private final GasPriceService gasPriceService;
    private final AddressTransferService addressTransferService;
    private final ValueTransferService valueTransferService;
    private final SessionStatisticsService statsService;
    private final TerminalDisplay display;
    private final Scanner scanner;
    private final ConsoleInputValidator validator;

    public UICommandHandler(BlockchainDataService dataService, GasPriceService gasPriceService,
                            AddressTransferService addressTransferService, ValueTransferService valueTransferService,
                            SessionStatisticsService statsService, TerminalDisplay display,
                            Scanner scanner, ConsoleInputValidator validator) {
        this.dataService = dataService;
        this.gasPriceService = gasPriceService;
        this.addressTransferService = addressTransferService;
        this.valueTransferService = valueTransferService;
        this.statsService = statsService;
        this.display = display;
        this.scanner = scanner;
        this.validator = validator;
    }

    public void handleBlockReport() {
        System.out.println(TerminalColorizer.yellow("Pobieranie 100 bloków..."));
        List<BlockDTO> blocks = dataService.fetchLatestBlocksData();
        for (BlockDTO block : blocks) {
            System.out.println(TerminalColorizer.cyan("\n" + "#".repeat(FormatConstants.TABLE_WIDTH)));
            System.out.println(TerminalColorizer.green(String.format("BLOK: %d | Hash: %s | Ilość Tx: %d",
                    block.number(), HashShortener.shorten(block.hash()), block.transactionCount())));
            if (block.transactions() != null && !block.transactions().isEmpty()) {
                TableFormatter.printTransactionsTable(block.transactions());
            }
        }
        display.waitForEnter(scanner);
    }

    public void handleRealTimeMonitor() {
        dataService.monitorRealTime();
    }

    public void handleGasPriceCalculation() {
        try {
            BigDecimal avg = gasPriceService.calculateAverageGasPriceFor100Blocks();
            System.out.println(TerminalColorizer.green("\nŚrednia cena Gas: " + avg.toPlainString() + " Wei"));
        } catch (Exception e) { logger.error("Błąd: ", e); }
        display.waitForEnter(scanner);
    }

    public void handleReportGenerationToFile() {
        try {
            BigDecimal avg = gasPriceService.calculateAverageGasPriceFor100Blocks();
            List<BlockDTO> blocks = dataService.fetchLatestBlocksData();
            ReportGenerator.generateTxtReport(blocks, avg);
        } catch (Exception e) { logger.error("Błąd: ", e); }
        display.waitForEnter(scanner);
    }

    public void handleFilterSubmenu() {
        System.out.println(TerminalColorizer.cyan("\n--- WYBÓR WYJŚCIA RAPORTU ---"));
        System.out.println("1. Wyświetl w konsoli");
        System.out.println("2. Zapisz do pliku .txt");
        System.out.println("0. Powrót"); // Opcja powrotu

        int outputChoice = validator.getValidInt("Wybór (0-2): ", 0, 2);
        if (outputChoice == 0) return; // Wychodzimy z metody, wracamy do pętli start()

        boolean toFile = (outputChoice == 2);

        System.out.println(TerminalColorizer.cyan("\n--- TYP FILTROWANIA ---"));
        System.out.println("1. Po adresie portfela");
        System.out.println("2. Po wartości ETH (>= próg)");
        System.out.println("0. Powrót");

        int filterType = validator.getValidInt("Wybór (0-2): ", 0, 2);
        if (filterType == 0) return;

        if (filterType == 1) {
            handleAddressFilter(toFile);
        } else {
            handleValueFilter(toFile);
        }
        display.waitForEnter(scanner);
    }

    private void handleAddressFilter(boolean toFile) {
        System.out.print("Podaj adres portfela (0x...) [lub naciśnij 0 aby wrócić]: ");
        String addr = scanner.nextLine().trim();

        // "Bezpieczne wyjście" - jeśli puste lub 0
        if (addr.isEmpty() || addr.equals("0")) {
            System.out.println(TerminalColorizer.yellow("Anulowano operację."));
            return;
        }

        if (!AddressValidator.isValid(addr)) {
            System.out.println(TerminalColorizer.red("Błędny format adresu!"));
            return;
        }

        System.out.println("Tryb: 1. Od | 2. Do | 3. Od/Do [0. Anuluj]");
        int modeChoice = validator.getValidInt("Wybór: ", 0, 3);
        if (modeChoice == 0) return;

        AddressMatchMode mode = switch (modeChoice) {
            case 1 -> AddressMatchMode.FROM;
            case 2 -> AddressMatchMode.TO;
            default -> AddressMatchMode.FROM_OR_TO;
        };

        int limit = validator.getValidInt("Limit transakcji (1-100): ", 1, 100);

        try {
            List<AddressTransferDTO> res = addressTransferService.findLatest(AddressValidator.normalize(addr), mode, limit);
            statsService.recordFilteredResults(res.size(), res);
            if (toFile) {
                ReportGenerator.generateAddressTransferReport(addr, mode, res);
            } else {
                AddressTransferFormatter.printTable(res);
            }
        } catch (Exception e) { logger.error("Błąd filtrowania: ", e); }
    }

    private void handleValueFilter(boolean toFile) {
        System.out.print("Minimalna wartość ETH [lub naciśnij Enter/0 aby wrócić]: ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty() || input.equals("0")) {
            System.out.println(TerminalColorizer.yellow("Anulowano operację."));
            return;
        }

        try {
            BigDecimal min = new BigDecimal(input.replace(",", "."));
            int limit = validator.getValidInt("Limit transakcji (1-100): ", 1, 100);
            List<AddressTransferDTO> res = valueTransferService.findLatestAbove(min, limit);
            statsService.recordFilteredResults(res.size(), res);

            if (toFile) {
                ReportGenerator.generateValueTransferReport(min, limit, res);
            } else {
                AddressTransferFormatter.printTable(res);
            }
        } catch (Exception e) { System.out.println(TerminalColorizer.red("Błędna liczba!")); }
    }

    public void handleExit() {
        System.out.println(TerminalColorizer.green("\n" + "=".repeat(50)));
        System.out.println(TerminalColorizer.green("       FINALNY RAPORT PODSUMOWUJĄCY SESJĘ"));
        System.out.println(TerminalColorizer.green("=".repeat(50)));
        System.out.println("Czas pracy:         " + statsService.getSessionDuration());
        System.out.println("Przetworzone dane:  " + statsService.getFormattedStats());
        System.out.println("Łączna wartość ETH: " + statsService.getTotalValueEth().setScale(6, java.math.RoundingMode.HALF_UP).toPlainString() + " ETH");
        System.out.println(TerminalColorizer.green("=".repeat(50)));
        System.exit(0);
    }
}