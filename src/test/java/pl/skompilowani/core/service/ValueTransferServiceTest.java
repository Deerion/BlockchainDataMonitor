package pl.skompilowani.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.EthBlock;
import pl.skompilowani.core.model.AddressTransferDTO;
import pl.skompilowani.infrastructure.client.BlockchainClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("ValueTransferService - Testy filtrowania progów wartości")
class ValueTransferServiceTest {

    private BlockchainClient mockClient;
    private ValueTransferService valueTransferService;

    @BeforeEach
    void setUp() {
        mockClient = Mockito.mock(BlockchainClient.class);
        valueTransferService = new ValueTransferService(mockClient);
    }

    @Test
    @DisplayName("Powinien przefiltrować i odrzucić transakcje poniżej zadanego progu ETH")
    void shouldFilterTransactionsBelowThreshold() throws Exception {
        // Given
        BigInteger targetBlockNum = BigInteger.valueOf(100);
        when(mockClient.getLatestBlockNumber()).thenReturn(targetBlockNum);

        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x64"); // 100
        mockBlock.setTimestamp("0x66440000");

        EthBlock.TransactionObject txLow = new EthBlock.TransactionObject();
        txLow.setHash("0xlow");
        txLow.setValue("0x6f05b59d3b20000"); // 0.5 ETH

        EthBlock.TransactionObject txHigh = new EthBlock.TransactionObject();
        txHigh.setHash("0xhigh");
        txHigh.setValue("0xde0b6b3a7640000"); // 1.0 ETH

        mockBlock.setTransactions(Arrays.asList(
                (EthBlock.TransactionResult<EthBlock.TransactionObject>) () -> txLow,
                (EthBlock.TransactionResult<EthBlock.TransactionObject>) () -> txHigh
        ));

        when(mockClient.getBlockDetails(targetBlockNum)).thenReturn(mockBlock);

        // When:
        List<AddressTransferDTO> result = valueTransferService.findLatestAbove(BigDecimal.ONE, 10);

        // Then
        assertEquals(1, result.size(), "Oczekiwano dokładnie jednej transakcji powyżej progu 1 ETH");
        assertEquals("0xhigh", result.get(0).hash());
    }
}