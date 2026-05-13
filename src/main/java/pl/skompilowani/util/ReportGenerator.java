package pl.skompilowani.util;

import pl.skompilowani.service.SessionStatisticsService;
import pl.skompilowani.service.dto.AddressTransferDTO;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import pl.skompilowani.service.UnitConverter;
import pl.skompilowani.service.filter.AddressMatchMode;

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

    // Zwiększono szerokość dla pełnej daty i godziny
    private static final int FILE_WIDTH = 230;

    public static void generateTxtReport(List<BlockDTO> blocks, BigDecimal avgGasPrice, SessionStatisticsService stats) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_blockchain_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(center("ZAAWANSOWANY RAPORT ANALITYCZNY BLOCKCHAIN (SIEĆ SEPOLIA)", FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(padLeft("Data generowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), FILE_WIDTH) + "\n");
            writer.write("-".repeat(FILE_WIDTH) + "\n");

            writer.write(center("PODSUMOWANIE STATYSTYCZNE ZESTAWU DANYCH", FILE_WIDTH) + "\n");
            writer.write("-".repeat(FILE_WIDTH) + "\n");
            writer.write(String.format("Liczba przeanalizowanych bloków: %d\n", blocks.size()));

            if (avgGasPrice != null) {
                BigDecimal avgGwei = UnitConverter.weiToGwei(avgGasPrice);
                writer.write(String.format("Średnia cena Gas (BaseFee):      %s Wei (%s Gwei)\n",
                        avgGasPrice.toPlainString(), avgGwei.toPlainString()));
            }

            int totalTxsInReport = blocks.stream().mapToInt(BlockDTO::transactionCount).sum();
            writer.write(String.format("Łączna liczba transakcji w raporcie: %d\n", totalTxsInReport));
            writer.write(String.format("Czas trwania sesji monitoringu:     %s\n", stats.getSessionDuration()));
            writer.write("=".repeat(FILE_WIDTH) + "\n\n");

            for (BlockDTO block : blocks) {
                writer.write("#".repeat(FILE_WIDTH) + "\n");
                writer.write(center(String.format("BLOK: %d | Hash: %s | Ilość Tx: %d", block.number(), block.hash(), block.transactionCount()), FILE_WIDTH) + "\n");
                writer.write("#".repeat(FILE_WIDTH) + "\n");

                if (block.transactions() != null && !block.transactions().isEmpty()) {
                    writer.write("-".repeat(FILE_WIDTH) + "\n");
                    // Zmieniono ostatnią kolumnę na %-20s
                    writer.write(String.format("| %-66s | %-42s | %-42s | %-12s | %-10s | %-12s | %-20s |\n",
                            "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data i Czas"));
                    writer.write("-".repeat(FILE_WIDTH) + "\n");

                    for (TransactionDTO tx : block.transactions()) {
                        // Usunięto .substring(0, 10)
                        writer.write(String.format("| %-66s | %-42s | %-42s | %-12.6f | %-10d | %-12.6f | %-20s |\n",
                                tx.hash(), tx.from(), tx.to() != null ? tx.to() : "Tworzenie Kontaktu",
                                tx.valueEth(), tx.gasUsed(), tx.oplataEth(),
                                DateFormatter.format(tx.timestamp())));
                    }
                    writer.write("-".repeat(FILE_WIDTH) + "\n\n");
                }
            }
            System.out.println(TerminalColorizer.green("\nSukces! Profesjonalny raport zapisany: " + filePath.toAbsolutePath()));
        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd zapisu: " + e.getMessage()));
        }
    }

    public static void generateValueTransferReport(BigDecimal minValueEth, int limit, List<AddressTransferDTO> transfers) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_wartosc_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(center("RAPORT TRANSAKCJI WG WARTOŚCI (SIEĆ ETH Sepolia)", FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(padLeft("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), FILE_WIDTH) + "\n");
            writer.write(padLeft("Minimalna wartość: " + minValueEth.toPlainString() + " ETH", FILE_WIDTH) + "\n");
            writer.write(padLeft("Liczba wyników: " + limit, FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n\n");

            AddressTransferFormatter.writeTable(writer, transfers);
            System.out.println(TerminalColorizer.green("\nSukces! Raport zapisany w pliku: " + filePath.toAbsolutePath()));
        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd podczas zapisu pliku: " + e.getMessage()));
        }
    }

    public static void generateAddressTransferReport(String address, AddressMatchMode mode, List<AddressTransferDTO> transfers) {
        String shortAddr = HashShortener.shorten(address);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_adres_" + shortAddr.replaceAll("[^a-zA-Z0-9]", "") + "_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(center("RAPORT TRANSAKCJI ADRESU PORTFELA (SIEĆ ETH Sepolia)", FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(padLeft("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), FILE_WIDTH) + "\n");
            writer.write(padLeft("Adres: " + address, FILE_WIDTH) + "\n");
            writer.write(padLeft("Tryb filtrowania: " + mode.label(), FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n\n");

            AddressTransferFormatter.writeTable(writer, transfers);
            System.out.println(TerminalColorizer.green("\nSukces! Raport zapisany w pliku: " + filePath.toAbsolutePath()));
        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd podczas zapisu pliku: " + e.getMessage()));
        }
    }

    private static String center(String text, int width) {
        if (text == null) text = "";
        int padding = width - text.length();
        if (padding <= 0) return text;
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