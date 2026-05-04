package pl.skompilowani.util;

public class TerminalColorizer {

    // kody kolorów ANSI
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";

    /**
     * Zwraca tekst pokolorowany na czerwono (np. dla błędów).
     */
    public static String red(String text) {
        return ANSI_RED + text + ANSI_RESET;
    }

    /**
     * Zwraca tekst pokolorowany na zielono (np. dla sukcesów).
     */
    public static String green(String text) {
        return ANSI_GREEN + text + ANSI_RESET;
    }

    /**
     * Zwraca tekst pokolorowany na żółto (np. dla ostrzeżeń).
     */
    public static String yellow(String text) {
        return ANSI_YELLOW + text + ANSI_RESET;
    }

    /**
     * Zwraca tekst pokolorowany na niebiesko.
     */
    public static String cyan(String text) {
        return ANSI_CYAN + text + ANSI_RESET;
    }
}