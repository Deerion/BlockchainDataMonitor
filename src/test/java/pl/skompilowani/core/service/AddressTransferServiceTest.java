package pl.skompilowani.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.core.model.AddressMatchMode;
import pl.skompilowani.core.model.AddressTransferDTO;
import pl.skompilowani.infrastructure.client.BlockchainClient;
import pl.skompilowani.infrastructure.client.alchemy.AlchemyAssetTransferClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AddressTransferService - Testy logiki biznesowej filtrowania adresów")
class AddressTransferServiceTest {

    private BlockchainClient mockBlockchainClient;
    private AlchemyAssetTransferClient mockAlchemyClient;
    private AddressTransferService addressTransferService;

    private final String targetAddress = "0x1111111111111111111111111111111111111111";

    @BeforeEach
    void setUp() throws Exception {
        mockBlockchainClient = Mockito.mock(BlockchainClient.class);
        mockAlchemyClient = Mockito.mock(AlchemyAssetTransferClient.class);

        when(mockBlockchainClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(100000));

        addressTransferService = new AddressTransferService("http://localhost:8545", mockBlockchainClient);

        java.lang.reflect.Field clientField = AddressTransferService.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(addressTransferService, mockAlchemyClient);
    }

    @Test
    @DisplayName("Powinien poprawnie wyliczyć bezpieczne okno bloków (SAFE_BLOCK_WINDOW)")
    void shouldCalculateCorrectBlockWindow() throws Exception {
        // Given
        when(mockBlockchainClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(60000));

        String expectedFromBlockHex = "0x" + BigInteger.valueOf(10000).toString(16);

        when(mockAlchemyClient.getTransfersByFromAddress(anyString(), anyInt(), eq(expectedFromBlockHex)))
                .thenReturn(new ArrayList<>());

        // When
        List<AddressTransferDTO> result = addressTransferService.findLatest(targetAddress, AddressMatchMode.FROM, 10);

        // Then
        assertNotNull(result);
        verify(mockAlchemyClient).getTransfersByFromAddress(eq(targetAddress), eq(10), eq(expectedFromBlockHex));
    }

    @Test
    @DisplayName("Powinien ustawić blok startowy na 0x0, jeśli najnowszy blok jest mniejszy niż SAFE_BLOCK_WINDOW")
    void shouldFallbackToZeroBlockWhenLatestBlockIsSmall() throws Exception {
        // Given
        when(mockBlockchainClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(3000));
        String expectedFromBlockHex = "0x0";

        when(mockAlchemyClient.getTransfersByToAddress(anyString(), anyInt(), eq(expectedFromBlockHex)))
                .thenReturn(new ArrayList<>());

        // When
        List<AddressTransferDTO> result = addressTransferService.findLatest(targetAddress, AddressMatchMode.TO, 5);

        // Then
        assertNotNull(result);
        verify(mockAlchemyClient).getTransfersByToAddress(eq(targetAddress), eq(5), eq(expectedFromBlockHex));
    }

    @Test
    @DisplayName("Powinien połączyć i zdeduplikować transakcje w trybie FROM_OR_TO, sortując malejąco po numerze bloku")
    void shouldMergeAndDeduplicateTransfersInFromOrToMode() throws Exception {
        // Given
        String fromBlockHex = "0x" + BigInteger.valueOf(50000).toString(16);

        AddressTransferDTO tx1 = new AddressTransferDTO("0xhash1", targetAddress, "0xrecipient", BigInteger.valueOf(90000), BigDecimal.ONE, "ETH", "external", "2026-05-19T12:00:00Z", BigInteger.ZERO, BigDecimal.ZERO);
        AddressTransferDTO tx2 = new AddressTransferDTO("0xhash2", targetAddress, "0xrecipient2", BigInteger.valueOf(91000), BigDecimal.TEN, "ETH", "external", "2026-05-19T12:05:00Z", BigInteger.ZERO, BigDecimal.ZERO);

        AddressTransferDTO tx2Duplicate = new AddressTransferDTO("0xhash2", targetAddress, "0xrecipient2", BigInteger.valueOf(91000), BigDecimal.TEN, "ETH", "external", "2026-05-19T12:05:00Z", BigInteger.ZERO, BigDecimal.ZERO);
        AddressTransferDTO tx3 = new AddressTransferDTO("0xhash3", "0xsender", targetAddress, BigInteger.valueOf(92000), BigDecimal.ZERO, "ETH", "external", "2026-05-19T12:10:00Z", BigInteger.ZERO, BigDecimal.ZERO);

        when(mockAlchemyClient.getTransfersByFromAddress(eq(targetAddress), eq(10), eq(fromBlockHex)))
                .thenReturn(List.of(tx1, tx2));
        when(mockAlchemyClient.getTransfersByToAddress(eq(targetAddress), eq(10), eq(fromBlockHex)))
                .thenReturn(List.of(tx2Duplicate, tx3));

        when(mockBlockchainClient.getTransactionReceipt(anyString())).thenReturn(Optional.empty());

        // When
        List<AddressTransferDTO> result = addressTransferService.findLatest(targetAddress, AddressMatchMode.FROM_OR_TO, 10);

        // Then
        assertEquals(3, result.size(), "Zduplikowany hash transakcji powinien zostać usunięty");
        // Sprawdzenie kolejności sortowania malejąco po blockNumber (tx3=92000, tx2=91000, tx1=90000)
        assertEquals("0xhash3", result.get(0).hash());
        assertEquals("0xhash2", result.get(1).hash());
        assertEquals("0xhash1", result.get(2).hash());
    }

    @Test
    @DisplayName("Powinien poprawnie wzbogacić transakcje o opłaty za Gas pobrane z receipta")
    void shouldEnrichTransfersWithGasFees() throws Exception {
        // Given
        String fromBlockHex = "0x" + BigInteger.valueOf(50000).toString(16);
        AddressTransferDTO tx = new AddressTransferDTO("0xtx_enriched", targetAddress, "0xto", BigInteger.valueOf(95000), BigDecimal.ONE, "ETH", "external", "2026-05-19", BigInteger.ZERO, BigDecimal.ZERO);

        when(mockAlchemyClient.getTransfersByFromAddress(eq(targetAddress), eq(1), eq(fromBlockHex)))
                .thenReturn(List.of(tx));

        TransactionReceipt mockReceipt = new TransactionReceipt();
        mockReceipt.setGasUsed("0x5208"); // 21000
        mockReceipt.setEffectiveGasPrice("0x4A817C800"); // 20 Gwei (20000000000 Wei)

        when(mockBlockchainClient.getTransactionReceipt("0xtx_enriched")).thenReturn(Optional.of(mockReceipt));

        // When
        List<AddressTransferDTO> result = addressTransferService.findLatest(targetAddress, AddressMatchMode.FROM, 1);

        // Then
        assertEquals(1, result.size());
        AddressTransferDTO processed = result.get(0);

        // Oczekiwany koszt: 21000 * 20 000 000 000 = 420 000 000 000 000 Wei = 0.00042 ETH
        assertEquals(BigInteger.valueOf(21000), processed.gasUsed());
        assertEquals(new BigDecimal("0.000420000000000000"), processed.gasFeeEth());
    }
}