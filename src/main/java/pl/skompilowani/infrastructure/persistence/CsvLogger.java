package pl.skompilowani.infrastructure.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.core.model.TransactionDTO;
import pl.skompilowani.core.service.TransactionLogRepository;
import pl.skompilowani.shared.util.DateFormatter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class CsvLogger implements TransactionLogRepository {
    private static final Logger logger = LoggerFactory.getLogger(CsvLogger.class);
    private static final Path FILE_PATH = Paths.get("live_stream.csv");
    private static final String DELIMITER = ";";

    @Override
    public void logTransaction(BlockDTO block, TransactionDTO tx) {
        boolean isNewFile = !Files.exists(FILE_PATH);

        try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

            if (isNewFile) {
                writer.write(String.join(DELIMITER,
                        "Blok_Numer", "Blok_Hash", "Hash_TX", "Od", "Do", "Wartosc_ETH", "Gaz", "Data"));
                writer.newLine();
            }

            String line = String.format("%d%s%s%s%s%s%s%s%s%s%s%s%d%s%s",
                    block.number(), DELIMITER,
                    block.hash(), DELIMITER,
                    tx.hash(), DELIMITER,
                    tx.from(), DELIMITER,
                    (tx.to() != null ? tx.to() : "Tworzenie Kontraktu"), DELIMITER,
                    tx.valueEth().toString().replace(".", ","), DELIMITER,
                    tx.gasUsed(), DELIMITER,
                    DateFormatter.format(tx.timestamp())
            );

            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            logger.error("Błąd zapisu CSV: {}", e.getMessage());
        }
    }
}