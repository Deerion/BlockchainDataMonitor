package pl.skompilowani.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.EthBlock;
import pl.skompilowani.core.service.GasPriceService;
import pl.skompilowani.core.service.ProgressListener;
import pl.skompilowani.infrastructure.client.BlockchainClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class GasPriceServiceTest {

    @Test
    void shouldCalculateAverageCorrectly() throws IOException {
        // Given
        BlockchainClient mockClient = Mockito.mock(BlockchainClient.class);
        // Poprawka: Tworzymy czysty mock interfejsu zamiast wadliwej lambdy
        ProgressListener mockListener = Mockito.mock(ProgressListener.class);
        GasPriceService service = new GasPriceService(mockClient, mockListener);

        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x1");
        mockBlock.setBaseFeePerGas("0x3B9ACA00");

        mockBlock.setTransactions(java.util.Collections.emptyList());

        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.ONE);
        when(mockClient.getBlockDetails(any())).thenReturn(mockBlock);

        // When
        BigDecimal result = service.calculateAverageGasPriceFor100Blocks();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1000000000.00"), result);
    }
}