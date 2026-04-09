package pl.skompilowani.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.skompilowani.api.BlockchainClient;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class GasPriceService {
    private static final Logger logger = LoggerFactory.getLogger(GasPriceService.class);
    private final BlockchainClient client;

    public GasPriceService(BlockchainClient client) {
        this.client = client;
    }

    /**
     * Oblicza średnią cenę Gas (BaseFeePerGas) dla grupy 100 bloków.
     * Zaczyna od najnowszego dostępnego bloku i cofa się wstecz.
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
            BigInteger currentBlockNum = latestBlockNum.subtract(BigInteger.valueOf(i));
            if (currentBlockNum.compareTo(BigInteger.ZERO) < 0) break;

            EthBlock.Block block = client.getBlockDetails(currentBlockNum);
            if (block != null) {
                BigInteger baseFee = block.getBaseFeePerGas();
                if (baseFee != null) {
                    totalBaseFee = totalBaseFee.add(baseFee);
                    blocksFound++;
                } else {
                    logger.warn("Blok {} nie posiada baseFeePerGas. Pomijanie...", currentBlockNum);
                }
            }
        }

        if (blocksFound == 0) {
            logger.warn("Nie znaleziono żadnych bloków z baseFeePerGas w podanym zakresie.");
            return BigDecimal.ZERO;
        }

        BigDecimal average = new BigDecimal(totalBaseFee).divide(new BigDecimal(blocksFound), 2, RoundingMode.HALF_UP);
        logger.info("Obliczono średnią cenę Gas (BaseFee) dla {} bloków: {} Wei", blocksFound, average);
        return average;
    }
}
