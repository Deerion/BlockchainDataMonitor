package pl.skompilowani.presentation.console;

import pl.skompilowani.core.service.SessionStatisticsService;
import pl.skompilowani.shared.ui.TerminalColorizer;
import java.util.Scanner;

public class TerminalDisplay {

    public void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printLogo() {
        System.out.println(TerminalColorizer.cyan("   ███      ███") + TerminalColorizer.green("███      ███   "));
        System.out.println(TerminalColorizer.cyan("   ██    ███   ") + TerminalColorizer.green("   ███    ██   "));
        System.out.println(TerminalColorizer.cyan("   ██  ██     █") + TerminalColorizer.green("█     ██  ██   "));
        System.out.println(TerminalColorizer.cyan("   ██  █   ████") + TerminalColorizer.green("████   █  ██   "));
        System.out.println(TerminalColorizer.cyan(" ███   ██    ██") + TerminalColorizer.green("█  ████    ███ "));
        System.out.println(TerminalColorizer.cyan(" ███    ████   ") + TerminalColorizer.green(" ███  ██   ███ "));
        System.out.println(TerminalColorizer.cyan("   ██  █   ████") + TerminalColorizer.green("████   █  ██   "));
        System.out.println(TerminalColorizer.cyan("   ██  ██     █") + TerminalColorizer.green("█     ██  ██   "));
        System.out.println(TerminalColorizer.cyan("   ██    ███   ") + TerminalColorizer.green("   ███    ██   "));
        System.out.println(TerminalColorizer.cyan("   ███      ███") + TerminalColorizer.green("███      ███   "));
        System.out.println("\n         " + TerminalColorizer.cyan("SKOMPI") + TerminalColorizer.green("LOWANI\n"));
    }

    public void renderHeader(SessionStatisticsService stats) {
        System.out.println(TerminalColorizer.cyan("--------------------------------------------------"));
        System.out.println(TerminalColorizer.green(" STATUS: AKTYWNY"));
        System.out.println(TerminalColorizer.yellow(" PRZETWORZONO: " + stats.getFormattedStats()));
        System.out.println(TerminalColorizer.cyan("--------------------------------------------------"));
    }

    public void waitForEnter(Scanner scanner) {
        try {
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (Exception ignored) {}
        System.out.println(TerminalColorizer.yellow("\nNaciśnij [ENTER], aby kontynuować..."));
        scanner.nextLine();
    }
}