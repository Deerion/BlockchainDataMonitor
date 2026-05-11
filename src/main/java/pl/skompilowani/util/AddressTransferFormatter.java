package pl.skompilowani.util;

import pl.skompilowani.service.dto.AddressTransferDTO;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public final class AddressTransferFormatter {

    private static final int TABLE_WIDTH = FormatConstants.TABLE_WIDTH;

    // Identical column layout to TableFormatter: Hash(15)|Od(15)|Do(15)|Wartość ETH(12)|Zużyty gaz(10)|Opłata ETH(12)|Data(12) = 113
    private static final String HEADER_FORMAT = "| %-15s | %-15s | %-15s | %-12s | %-10s | %-12s | %-12s |";
    private static final String ROW_FORMAT    = "| %-15s | %-15s | %-15s | %-12.6f | %-10d | %-12.6f | %-12s |";

    private AddressTransferFormatter() {}

    public static void printTable(List<AddressTransferDTO> transfers) {
        if (transfers == null || transfers.isEmpty()) {
            System.out.println(TerminalColorizer.yellow("Brak transakcji spełniających podane kryteria."));
            return;
        }

        System.out.println("-".repeat(TABLE_WIDTH));
        System.out.println(String.format(HEADER_FORMAT,
                "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data"));
        System.out.println("-".repeat(TABLE_WIDTH));

        for (AddressTransferDTO t : transfers) {
            String toCell = t.to() != null ? t.to() : "Tworzenie Kontr.";
            System.out.println(String.format(ROW_FORMAT,
                    HashShortener.shorten(t.hash()),
                    HashShortener.shorten(t.from()),
                    HashShortener.shorten(toCell),
                    t.value(),
                    t.gasUsed(),
                    t.gasFeeEth(),
                    formatDate(t.blockTimestamp())));
        }

        System.out.println("-".repeat(TABLE_WIDTH));
    }

    public static void writeTable(BufferedWriter writer, List<AddressTransferDTO> transfers) throws IOException {
        if (transfers == null || transfers.isEmpty()) {
            writer.write("Brak transakcji spełniających podane kryteria.\n");
            return;
        }

        writer.write("-".repeat(TABLE_WIDTH) + "\n");
        writer.write(String.format(HEADER_FORMAT,
                "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data") + "\n");
        writer.write("-".repeat(TABLE_WIDTH) + "\n");

        for (AddressTransferDTO t : transfers) {
            String toCell = t.to() != null ? t.to() : "Tworzenie Kontr.";
            writer.write(String.format(ROW_FORMAT,
                    HashShortener.shorten(t.hash()),
                    HashShortener.shorten(t.from()),
                    HashShortener.shorten(toCell),
                    t.value(),
                    t.gasUsed(),
                    t.gasFeeEth(),
                    formatDate(t.blockTimestamp())) + "\n");
        }

        writer.write("-".repeat(TABLE_WIDTH) + "\n");
    }

    private static String formatDate(String blockTimestamp) {
        if (blockTimestamp == null || blockTimestamp.length() < 10) return "";
        return blockTimestamp.substring(0, 10);
    }
}
