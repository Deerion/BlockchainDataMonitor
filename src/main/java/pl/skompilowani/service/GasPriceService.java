package pl.skompilowani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.mapper.BlockchainMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Serwis odpowiedzialny za analizę cen gazu w sieci blockchain.
 * Wykorzystuje BlockchainClient do pobierania danych i BlockchainMapper do transformacji na DTO.
 */
public class GasPriceService {
    private static final Logger logger = LoggerFactory.getLogger(GasPriceService.class);
    private final BlockchainClient client;

    public GasPriceService(BlockchainClient client) {
        this.client = client;
    }

    /**
     * Oblicza średnią cenę Gas (BaseFeePerGas) dla grupy 100 bloków.
     * Wykorzystuje obiekty DTO, aby odizolować logikę od surowych danych biblioteki Web3j.
     *
     * @return Średnia cena Gas (BaseFeePerGas) w Wei.
     * @throws IOException W przypadku błędu komunikacji z blockchainem.
     */
    public BigDecimal calculateAverageGasPriceFor100Blocks() throws IOException {
        int blockCount = 100;
        BigInteger latestBlockNum = client.getLatestBlockNumber();
        BigInteger totalBaseFee = BigInteger.ZERO;
        int blocksFound = 0;

        logger.info("Rozpoczynanie obliczania średniej ceny Gas dla ostatnich {} bloków (od bloku {})...", blockCount, latestBlockNum);

        for (int i = 0; i < blockCount; i++) {
            printProgressBar(i + 1, blockCount);
            BigInteger currentBlockNum = latestBlockNum.subtract(BigInteger.valueOf(i));
            if (currentBlockNum.compareTo(BigInteger.ZERO) < 0) break;

            // Pobranie surowego bloku z warstwy dostępu
            EthBlock.Block rawBlock = client.getBlockDetails(currentBlockNum);

            // Konwersja na DTO przy użyciu Twojego mappera
            BlockDTO blockDto = BlockchainMapper.toBlockDTO(rawBlock);

            if (blockDto != null) {
                BigInteger baseFee = blockDto.baseFeePerGas();
                if (baseFee != null) {
                    totalBaseFee = totalBaseFee.add(baseFee);
                    blocksFound++;
                } else {
                    logger.warn("Blok {} (hash: {}) nie posiada baseFeePerGas. Pomijanie...", blockDto.number(), blockDto.hash());
                }
            }
        }
        System.out.println(); // Nowa linia po zakończeniu paska postępu

        if (blocksFound == 0) {
            logger.warn("Nie znaleziono żadnych bloków z baseFeePerGas w podanym zakresie.");
            return BigDecimal.ZERO;
        }

        BigDecimal average = new BigDecimal(totalBaseFee).divide(new BigDecimal(blocksFound), 2, RoundingMode.HALF_UP);
        logger.info("Obliczono średnią cenę Gas (BaseFee) dla {} bloków: {} Wei", blocksFound, average);
        return average;
    }

    /**
     * Wyświetla pasek postępu w konsoli.
     */
    private void printProgressBar(int current, int total) {
        int barLength = 20;
        double percentage = (double) current / total;
        int filledLength = (int) (barLength * percentage);

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("#");
            } else {
                bar.append("-");
            }
        }
        bar.append("] ").append((int) (percentage * 100)).append("%");

        System.out.print("\r" + bar.toString());
    }
}