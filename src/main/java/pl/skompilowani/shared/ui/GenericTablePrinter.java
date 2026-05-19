package pl.skompilowani.shared.ui;

import java.util.List;
import java.util.function.Function;

public class GenericTablePrinter<T> {
    private final int tableWidth;
    private final List<ColumnDefinition<T>> columns;

    public record ColumnDefinition<T>(
            String header,
            int width,
            Function<T, Object> extractor,
            String formatSpecifier
    ) {}

    public GenericTablePrinter(int tableWidth, List<ColumnDefinition<T>> columns) {
        this.tableWidth = tableWidth;
        this.columns = columns;
    }

    public void printTable(List<T> data) {
        if (data == null || data.isEmpty()) {
            System.out.println(TerminalColorizer.yellow("Brak danych do wyświetlenia."));
            return;
        }

        printBorder();
        printHeader();
        printBorder();

        for (T item : data) {
            printRow(item);
        }

        printBorder();
    }

    private void printHeader() {
        StringBuilder sb = new StringBuilder("|");
        for (var col : columns) {
            sb.append(String.format(" %-" + col.width + "s |", col.header));
        }
        System.out.println(sb.toString());
    }

    private void printRow(T item) {
        StringBuilder sb = new StringBuilder("|");
        for (var col : columns) {
            Object value = col.extractor.apply(item);
            String format = " %-" + col.width + col.formatSpecifier + " |";
            sb.append(String.format(format, value));
        }
        System.out.println(sb.toString());
    }

    private void printBorder() {
        System.out.println("-".repeat(tableWidth));
    }
}