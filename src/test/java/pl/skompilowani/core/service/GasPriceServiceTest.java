package pl.skompilowani.core.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.EthBlock;
import pl.skompilowani.infrastructure.client.BlockchainClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("GasPriceService - Testy kalkulacji średniej ceny Gas")
class GasPriceServiceTest {

    @Test
    @DisplayName("Powinien prawidłowo obliczyć średnią wartość baseFee z bloków")
    void shouldCalculateAverageGasPriceCorrectly() throws IOException {
        // Given
        BlockchainClient mockClient = Mockito.mock(BlockchainClient.class);
        ProgressListener mockListener = Mockito.mock(ProgressListener.class);
        GasPriceService service = new GasPriceService(mockClient, mockListener);

        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x1");
        mockBlock.setBaseFeePerGas("0x3B9ACA00"); // 1 000 000 000 Wei
        mockBlock.setTransactions(java.util.Collections.emptyList());

        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.ONE);
        when(mockClient.getBlockDetails(any())).thenReturn(mockBlock);

        // When
        BigDecimal result = service.calculateAverageGasPriceFor100Blocks();

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("1000000000.00"), result);
    }

    @Test
    @DisplayName("Powinien zwrócić ZERO, gdy żaden z pobranych bloków nie posiada parametru baseFeePerGas")
    void shouldReturnZeroWhenNoBlocksHaveBaseFee() throws IOException {
        // Given
        BlockchainClient mockClient = Mockito.mock(BlockchainClient.class);
        ProgressListener mockListener = Mockito.mock(ProgressListener.class);
        GasPriceService service = new GasPriceService(mockClient, mockListener);

        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.ONE);

        EthBlock.Block mockBlock = Mockito.mock(EthBlock.Block.class);
        when(mockBlock.getNumber()).thenReturn(BigInteger.ONE);
        when(mockBlock.getHash()).thenReturn("0xmockhash");
        when(mockBlock.getBaseFeePerGas()).thenReturn(null);
        when(mockBlock.getTransactions()).thenReturn(java.util.Collections.emptyList());

        when(mockClient.getBlockDetails(any())).thenReturn(mockBlock);

        // When
        BigDecimal result = service.calculateAverageGasPriceFor100Blocks();

        // Then
        assertEquals(BigDecimal.ZERO, result, "W przypadku braku danych o opłatach serwis musi zwrócić 0");
    }
}