package pl.skompilowani.util;

import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CsvLogger {
    private static final Path FILE_PATH = Paths.get("live_stream.csv");
    private static final String DELIMITER = ";";

    /**
     * Zapisuje transakcję do pliku CSV wraz z informacją o bloku, do którego należy.
     */
    public static void logTransaction(BlockDTO block, TransactionDTO tx) {
        boolean isNewFile = !Files.exists(FILE_PATH);

        try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            // Rozszerzony nagłówek o dane bloku
            if (isNewFile) {
                writer.write(String.join(DELIMITER,
                        "Blok_Numer", "Blok_Hash", "Hash_TX", "Od", "Do", "Wartosc_ETH", "Gaz", "Data"));
                writer.newLine();
            }

            // Zapis danych z uwzględnieniem pełnych hashy bloku i transakcji
            String line = String.format("%d%s%s%s%s%s%s%s%s%s%s%s%d%s%s",
                    block.number(), DELIMITER,             // Numer bloku
                    block.hash(), DELIMITER,               // Pełny hash bloku
                    tx.hash(), DELIMITER,                  // Pełny hash transakcji
                    tx.from(), DELIMITER,
                    (tx.to() != null ? tx.to() : "Tworzenie Kontraktu"), DELIMITER,
                    tx.valueEth().toString().replace(".", ","), DELIMITER,
                    tx.gasUsed(), DELIMITER,
                    DateFormatter.format(tx.timestamp())
            );

            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Błąd zapisu CSV: " + e.getMessage());
        }
    }
}