package pl.skompilowani.util;

import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.service.UnitConverter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportGenerator {

    public static void generateTxtReport(List<BlockDTO> blocks, BigDecimal avgGasPrice) {
        // Tworzymy unikalną nazwę pliku bazując na aktualnym czasie
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_blockchain_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            final int TABLE_WIDTH = pl.skompilowani.util.FormatConstants.TABLE_WIDTH;
            // Nagłówek raportu (wyrównany do szerokości TABLE_WIDTH)
            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(center("RAPORT DANYCH BLOCKCHAIN (SIEĆ SEPOLIA)", TABLE_WIDTH) + "\n");
            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(padLeft("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), TABLE_WIDTH) + "\n");

            if (avgGasPrice != null) {
                BigDecimal avgGasPriceGwei = UnitConverter.weiToGwei(avgGasPrice);
                writer.write("Średnia cena gazu (dla ostatnich 100 bloków): "
                        + avgGasPrice.toPlainString() + " Wei (" + avgGasPriceGwei.toPlainString() + " Gwei)\n");
            } else {
                writer.write("Średnia cena gazu: Brak danych\n");
            }
            writer.write("=".repeat(TABLE_WIDTH) + "\n\n");

            if (blocks == null || blocks.isEmpty()) {
                writer.write("Brak pobranych bloków do wyświetlenia.\n");
                System.out.println(TerminalColorizer.green("Pusty raport został wygenerowany i zapisany: " + filePath.toAbsolutePath()));
                return;
            }

            writer.write(center("SZCZEGÓŁY OSTATNICH BLOKÓW I TRANSAKCJI:", TABLE_WIDTH) + "\n\n");

            // Iterujemy po blokach i transakcjach
            for (BlockDTO block : blocks) {
                writer.write("#".repeat(TABLE_WIDTH) + "\n");
                String blockHeader = String.format("BLOK: %d | Hash: %s | Ilość Tx: %d",
                        block.number(), block.hash(), block.transactionCount());
                writer.write(center(blockHeader, TABLE_WIDTH) + "\n");
                writer.write("#".repeat(TABLE_WIDTH) + "\n");

                if (block.transactions() != null && !block.transactions().isEmpty()) {
                    writer.write("-".repeat(TABLE_WIDTH) + "\n");
                    writer.write(String.format("| %-15s | %-15s | %-15s | %-12s | %-10s | %-12s | %-12s |\n",
                            "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data"));
                    writer.write("-".repeat(TABLE_WIDTH) + "\n");

                    for (TransactionDTO tx : block.transactions()) {
                        String toAddress = tx.to() != null ? tx.to() : "Tworzenie Kontr.";
                        String row = String.format("| %-15s | %-15s | %-15s | %-12.6f | %-10d | %-12.6f | %-12s |\n",
                                HashShortener.shorten(tx.hash()),
                                HashShortener.shorten(tx.from()),
                                HashShortener.shorten(toAddress),
                                tx.valueEth(),
                                tx.gasUsed(),
                                tx.oplataEth(),
                                DateFormatter.format(tx.timestamp()).substring(0, 10));
                        writer.write(row);
                    }
                    writer.write("-".repeat(TABLE_WIDTH) + "\n\n");
                } else {
                    writer.write("Brak transakcji w tym bloku spełniających kryteria.\n\n");
                }
            }

            System.out.println(TerminalColorizer.green("\nSukces! Raport zapisany w pliku: " + filePath.toAbsolutePath()));

        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd podczas zapisu pliku: " + e.getMessage()));
        }
    }

    private static String center(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static String padLeft(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text.substring(0, width);
        return text + " ".repeat(width - text.length());
    }
}