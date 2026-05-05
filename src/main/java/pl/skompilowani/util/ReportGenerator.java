package pl.skompilowani.util;

import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;

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
            // Nagłówek raportu
            writer.write("===============================================================================================\n");
            writer.write("                        RAPORT DANYCH BLOCKCHAIN (SIEĆ SEPOLIA)\n");
            writer.write("===============================================================================================\n");
            writer.write("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n");

            if (avgGasPrice != null) {
                writer.write("Średnia cena Gas (dla ostatnich 100 bloków): " + avgGasPrice + " Wei\n");
            } else {
                writer.write("Średnia cena Gas: Brak danych\n");
            }
            writer.write("===============================================================================================\n\n");

            if (blocks == null || blocks.isEmpty()) {
                writer.write("Brak pobranych bloków do wyświetlenia.\n");
                System.out.println(TerminalColorizer.green("Pusty raport został wygenerowany i zapisany: " + filePath.toAbsolutePath()));
                return;
            }

            writer.write("SZCZEGÓŁY OSTATNICH BLOKÓW I TRANSAKCJI:\n\n");

            // Iterujemy po blokach i transakcjach
            for (BlockDTO block : blocks) {
                writer.write("###############################################################################################\n");
                writer.write(String.format("BLOK: %d | Hash: %s | Ilość Tx: %d\n",
                        block.number(), block.hash(), block.transactionCount()));
                writer.write("###############################################################################################\n");

                if (block.transactions() != null && !block.transactions().isEmpty()) {
                    writer.write("-".repeat(95) + "\n");
                    writer.write(String.format("| %-15s | %-15s | %-15s | %-12s | %-10s | %-12s |\n",
                            "Hash", "Od", "Do", "Wartość ETH", "Gas", "Data"));
                    writer.write("-".repeat(95) + "\n");

                    for (TransactionDTO tx : block.transactions()) {
                        String toAddress = tx.to() != null ? tx.to() : "Tworzenie Kontr.";
                        String row = String.format("| %-15s | %-15s | %-15s | %-12.6f | %-10d | %-12s |\n",
                                HashShortener.shorten(tx.hash()),
                                HashShortener.shorten(tx.from()),
                                HashShortener.shorten(toAddress),
                                tx.valueEth(),
                                tx.gasUsed(),
                                DateFormatter.format(tx.timestamp()).substring(0, 10));
                        writer.write(row);
                    }
                    writer.write("-".repeat(95) + "\n\n");
                } else {
                    writer.write("Brak transakcji w tym bloku spełniających kryteria.\n\n");
                }
            }

            System.out.println(TerminalColorizer.green("\nSukces! Raport zapisany w pliku: " + filePath.toAbsolutePath()));

        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd podczas zapisu pliku: " + e.getMessage()));
        }
    }
}