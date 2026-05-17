package pl.skompilowani.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.infrastructure.mapper.BlockchainMapper;
import pl.skompilowani.shared.util.UnitConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class GasPriceService {
    private static final Logger logger = LoggerFactory.getLogger(GasPriceService.class);
    private final BlockchainClient client;
    private final ProgressListener progressListener;

    public GasPriceService(BlockchainClient client, ProgressListener progressListener) {
        this.client = client;
        this.progressListener = progressListener;
    }

    public BigDecimal calculateAverageGasPriceFor100Blocks() throws IOException {
        int blockCount = 100;
        BigInteger latestBlockNum = client.getLatestBlockNumber();
        BigInteger totalBaseFee = BigInteger.ZERO;
        int blocksFound = 0;

        logger.info("Rozpoczynanie obliczania średniej ceny Gas dla ostatnich {} bloków (od bloku {})...", blockCount, latestBlockNum);

        for (int i = 0; i < blockCount; i++) {
            progressListener.onProgress(i + 1, blockCount, "Przetwarzanie bloków do wyliczenia ceny gazu...");
            BigInteger currentBlockNum = latestBlockNum.subtract(BigInteger.valueOf(i));
            if (currentBlockNum.compareTo(BigInteger.ZERO) < 0) break;

            EthBlock.Block rawBlock = client.getBlockDetails(currentBlockNum);
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

        progressListener.onProgressComplete();

        if (blocksFound == 0) {
            logger.warn("Nie znaleziono żadnych bloków z baseFeePerGas w podanym zakresie.");
            return BigDecimal.ZERO;
        }

        BigDecimal average = new BigDecimal(totalBaseFee).divide(new BigDecimal(blocksFound), 2, RoundingMode.HALF_UP);
        BigDecimal averageGwei = UnitConverter.weiToGwei(average);
        logger.info("Obliczono średnią cenę Gas (BaseFee) dla {} bloków: {} Wei ({} Gwei)",
                blocksFound,
                average.toPlainString(),
                averageGwei.toPlainString());
        return average;
    }
}