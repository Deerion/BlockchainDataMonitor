package pl.skompilowani.util;

import pl.skompilowani.service.dto.AddressTransferDTO;
import java.util.List;
import java.io.BufferedWriter;
import java.io.IOException;

public final class AddressTransferFormatter {

    private static final GenericTablePrinter<AddressTransferDTO> CONSOLE_PRINTER = new GenericTablePrinter<>(
            FormatConstants.TABLE_WIDTH,
            List.of(
                    new GenericTablePrinter.ColumnDefinition<>("Hash", 15, t -> HashShortener.shorten(t.hash()), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Od", 15, t -> HashShortener.shorten(t.from()), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Do", 15, t -> HashShortener.shorten(t.to() != null ? t.to() : "Tworzenie Kontr."), "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Wartość ETH", 12, AddressTransferDTO::value, ".6f"),
                    new GenericTablePrinter.ColumnDefinition<>("Zużyty gaz", 10, AddressTransferDTO::gasUsed, "s"),
                    new GenericTablePrinter.ColumnDefinition<>("Opłata ETH", 12, AddressTransferDTO::gasFeeEth, ".6f"),
                    new GenericTablePrinter.ColumnDefinition<>("Data", 12, t -> t.blockTimestamp() != null && t.blockTimestamp().length() >= 10 ? t.blockTimestamp().substring(0, 10) : "", "s")
            )
    );

    // Definicja formatu specjalnie dla PLIKU (szersze kolumny dla pełnych danych)
    private static final int FILE_WIDTH = 220; // Zwiększona szerokość dla pełnych hashy
    private static final String FILE_HEADER_FORMAT = "| %-66s | %-42s | %-42s | %-12s | %-10s | %-12s | %-12s |";
    private static final String FILE_ROW_FORMAT    = "| %-66s | %-42s | %-42s | %-12.6f | %-10s | %-12.6f | %-12s |";

    private AddressTransferFormatter() {}

    public static void printTable(List<AddressTransferDTO> transfers) {
        CONSOLE_PRINTER.printTable(transfers);
    }

    public static void writeTable(BufferedWriter writer, List<AddressTransferDTO> transfers) throws IOException {
        if (transfers == null || transfers.isEmpty()) {
            writer.write("Brak transakcji spełniających podane kryteria.\n");
            return;
        }
        writer.write("-".repeat(FILE_WIDTH) + "\n");
        writer.write(String.format(FILE_HEADER_FORMAT, "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data") + "\n");
        writer.write("-".repeat(FILE_WIDTH) + "\n");

        for (AddressTransferDTO t : transfers) {
            String toCell = t.to() != null ? t.to() : "Tworzenie Kontaktu";
            // Zauważ brak HashShortener.shorten() - zapisujemy pełne dane
            writer.write(String.format(FILE_ROW_FORMAT,
                    t.hash(), t.from(), toCell, t.value(), t.gasUsed(), t.gasFeeEth(),
                    t.blockTimestamp() != null && t.blockTimestamp().length() >= 10 ? t.blockTimestamp().substring(0, 10) : "") + "\n");
        }
        writer.write("-".repeat(FILE_WIDTH) + "\n");
    }
}