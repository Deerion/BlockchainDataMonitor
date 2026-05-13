package pl.skompilowani.util;

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

    public static void generateTxtReport(List<BlockDTO> blocks, BigDecimal avgGasPrice) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_blockchain_" + timestamp + ".txt");
        final int FILE_WIDTH = 220;

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write("=".repeat(FILE_WIDTH) + "\n");
            writer.write(center("RAPORT DANYCH BLOCKCHAIN - PEŁNE DANE (SIEĆ SEPOLIA)", FILE_WIDTH) + "\n");
            writer.write("=".repeat(FILE_WIDTH) + "\n");


            for (BlockDTO block : blocks) {
                writer.write("#".repeat(FILE_WIDTH) + "\n");
                writer.write(center(String.format("BLOK: %d | Hash: %s | Ilość Tx: %d", block.number(), block.hash(), block.transactionCount()), FILE_WIDTH) + "\n");
                writer.write("#".repeat(FILE_WIDTH) + "\n");

                if (block.transactions() != null && !block.transactions().isEmpty()) {
                    writer.write("-".repeat(FILE_WIDTH) + "\n");
                    writer.write(String.format("| %-66s | %-42s | %-42s | %-12s | %-10s | %-12s | %-12s |\n",
                            "Hash", "Od", "Do", "Wartość ETH", "Zużyty gaz", "Opłata ETH", "Data"));
                    writer.write("-".repeat(FILE_WIDTH) + "\n");

                    for (TransactionDTO tx : block.transactions()) {
                        writer.write(String.format("| %-66s | %-42s | %-42s | %-12.6f | %-10d | %-12.6f | %-12s |\n",
                                tx.hash(), tx.from(), tx.to() != null ? tx.to() : "Contract Creation",
                                tx.valueEth(), tx.gasUsed(), tx.oplataEth(),
                                DateFormatter.format(tx.timestamp()).substring(0, 10)));
                    }
                    writer.write("-".repeat(FILE_WIDTH) + "\n\n");
                }
            }
            System.out.println(TerminalColorizer.green("\nSukces! Pełny raport zapisany: " + filePath.toAbsolutePath()));
        } catch (IOException e) {
            System.out.println(TerminalColorizer.red("Błąd zapisu: " + e.getMessage()));
        }
    }
    public static void generateValueTransferReport(BigDecimal minValueEth, int limit, List<AddressTransferDTO> transfers) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path filePath = Paths.get("raport_wartosc_" + timestamp + ".txt");

        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            final int TABLE_WIDTH = FormatConstants.TABLE_WIDTH;

            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(center("RAPORT TRANSAKCJI WG WARTOŚCI (SIEĆ ETH Sepolia)", TABLE_WIDTH) + "\n");
            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(padLeft("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), TABLE_WIDTH) + "\n");
            writer.write(padLeft("Minimalna wartość: " + minValueEth.toPlainString() + " ETH", TABLE_WIDTH) + "\n");
            writer.write(padLeft("Liczba wyników: " + limit, TABLE_WIDTH) + "\n");
            writer.write("=".repeat(TABLE_WIDTH) + "\n\n");

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
            final int TABLE_WIDTH = FormatConstants.TABLE_WIDTH;

            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(center("RAPORT TRANSAKCJI ADRESU PORTFELA (SIEĆ ETH Sepolia)", TABLE_WIDTH) + "\n");
            writer.write("=".repeat(TABLE_WIDTH) + "\n");
            writer.write(padLeft("Data wygenerowania: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), TABLE_WIDTH) + "\n");
            writer.write(padLeft("Adres: " + address, TABLE_WIDTH) + "\n");
            writer.write(padLeft("Tryb filtrowania: " + mode.label(), TABLE_WIDTH) + "\n");
            writer.write("=".repeat(TABLE_WIDTH) + "\n\n");

            AddressTransferFormatter.writeTable(writer, transfers);

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