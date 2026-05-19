package pl.skompilowani.shared.ui;

import pl.skompilowani.core.model.AddressTransferDTO;
import pl.skompilowani.shared.util.FormatConstants;
import pl.skompilowani.shared.util.HashShortener;

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
                    new GenericTablePrinter.ColumnDefinition<>("Data", 20, t -> formatTimestamp(t.blockTimestamp()), "s")
            )
    );

    private static final int FILE_WIDTH = 230;
    private static final String FILE_HEADER_FORMAT = "| %-66s | %-42s | %-42s | %-12s | %-10s | %-12s | %-20s |";
    private static final String FILE_ROW_FORMAT    = "| %-66s | %-42s | %-42s | %-12.6f | %-10s | %-12.6f | %-20s |";

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
        writer.write(String.format(FILE_HEADER_FORMAT, "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data i Czas") + "\n");
        writer.write("-".repeat(FILE_WIDTH) + "\n");

        for (AddressTransferDTO t : transfers) {
            String toCell = t.to() != null ? t.to() : "Tworzenie Kontaktu";
            writer.write(String.format(FILE_ROW_FORMAT,
                    t.hash(), t.from(), toCell, t.value(), t.gasUsed(), t.gasFeeEth(),
                    formatTimestamp(t.blockTimestamp())) + "\n");
        }
        writer.write("-".repeat(FILE_WIDTH) + "\n");
    }

    private static String formatTimestamp(String ts) {
        if (ts == null || ts.length() < 19) return ts != null ? ts : "";
        return ts.replace("T", " ").replace("Z", "").substring(0, 19);
    }
}