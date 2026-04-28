package pl.skompilowani;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.GasPriceService;
import pl.skompilowani.service.dto.BlockDTO; // Dodany import
import pl.skompilowani.service.mapper.BlockchainMapper; // Dodany import

import java.math.BigDecimal;
import java.math.BigInteger;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Uruchamianie Monitora Danych Blockchain...");

        Dotenv dotenv = Dotenv.load();
        String url = dotenv.get("BLOCKCHAIN_URL");

        if (url == null || url.isEmpty()) {
            logger.error("BŁĄD: Nie znaleziono zmiennej BLOCKCHAIN_URL w pliku .env!");
            return;
        }

        BlockchainClient client = new BlockchainClient(url);
        GasPriceService gasPriceService = new GasPriceService(client);

        logger.info("Sprawdzanie statusu sieci Sepolia...");
        if (!client.checkNetworkStatus()) {
            logger.error("Aplikacja kończy działanie z powodu braku połączenia z siecią.");
            return;
        }

        try {
            logger.info("Rozpoczynanie pobierania danych...");
            BigInteger latestNum = client.getLatestBlockNumber();
            logger.info("Sukces! Najnowszy numer bloku: {}", latestNum);

            // ZMIANA: Pobieramy surowe dane, ale natychmiast mapujemy je na DTO
            var rawBlock = client.getBlockDetails(latestNum);
            BlockDTO blockDto = BlockchainMapper.toBlockDTO(rawBlock);

            if (blockDto != null) {
                // Teraz logujemy dane korzystając z naszego DTO, a nie bezpośrednio z Web3j
                logger.info("Dane bloku -> Hash: {}, Liczba transakcji: {}",
                        blockDto.hash(), blockDto.transactionCount());
            }

            // Obliczanie średniej ceny Gas (ta metoda już wewnątrz używa DTO)
            BigDecimal averageGasPrice = gasPriceService.calculateAverageGasPriceFor100Blocks();
            logger.info("Średnia cena Gas dla ostatnich 100 bloków: {} Wei", averageGasPrice);

        } catch (Exception e) {
            logger.error("Wystąpił błąd podczas komunikacji z blockchainem: ", e);
        }
    }
}