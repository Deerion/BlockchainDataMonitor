package pl.skompilowani.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.infrastructure.client.BlockchainClient;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("BlockchainDataService - Testy integracji danych historycznych i Live")
class BlockchainDataServiceTest {

    private BlockchainClient mockClient;
    private SessionStatisticsService mockStats;
    private TransactionLogRepository mockRepo;
    private ProgressListener mockListener;
    private BlockchainDataService dataService;

    @BeforeEach
    void setUp() {
        mockClient = Mockito.mock(BlockchainClient.class);
        mockStats = Mockito.mock(SessionStatisticsService.class);
        mockRepo = Mockito.mock(TransactionLogRepository.class);
        mockListener = Mockito.mock(ProgressListener.class);

        dataService = new BlockchainDataService(mockClient, mockStats, mockRepo, mockListener);
    }

    @Test
    @DisplayName("Powinien pobrać i przetworzyć historię bloków przy niskim indeksie startowym")
    void shouldFetchLatestBlocksDataWithLowIndices() throws Exception {
        // Given
        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(1));

        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0xblockhash");
        rawBlock.setBaseFeePerGas("0x0");
        rawBlock.setTransactions(Collections.emptyList());
        when(mockClient.getBlockDetails(any(BigInteger.class))).thenReturn(rawBlock);

        // When
        List<BlockDTO> result = dataService.fetchLatestBlocksData();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        verify(mockListener, atLeastOnce()).onProgress(anyInt(), anyInt(), anyString());
        verify(mockListener).onProgressComplete();
        verify(mockStats, atLeastOnce()).recordBlock(anyInt());
    }

    @Test
    @DisplayName("Powinien przetworzyć transakcje z bloku i pobrać ich paragony (Receipts)")
    void shouldProcessBlockTransactionsAndFetchReceipts() throws Exception {
        // Given
        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(9));

        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x9");
        rawBlock.setHash("0xhash9");
        rawBlock.setBaseFeePerGas("0x0");
        rawBlock.setTimestamp("0x66440000");

        EthBlock.TransactionObject rawTx = new EthBlock.TransactionObject();
        rawTx.setHash("0xtxhash");
        rawTx.setFrom("0xfrom");
        rawTx.setTo("0xto");
        rawTx.setValue("0xde0b6b3a7640000"); // 1 ETH

        EthBlock.TransactionResult<EthBlock.TransactionObject> txResult = () -> rawTx;
        rawBlock.setTransactions(List.of(txResult));

        when(mockClient.getBlockDetails(BigInteger.valueOf(9))).thenReturn(rawBlock);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setGasUsed("0x5208"); // 21000
        receipt.setEffectiveGasPrice("0x3b9aca00"); // 1 Gwei
        when(mockClient.getTransactionReceipt("0xtxhash")).thenReturn(Optional.of(receipt));

        // When
        List<BlockDTO> result = dataService.fetchLatestBlocksData();

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        BlockDTO blockDto = result.get(result.size() - 1);
        assertEquals(1, blockDto.transactions().size());
        verify(mockStats).recordTransactionValue(eq(new BigDecimal("1.000000000000000000")), eq("0xtxhash"));    }

    @Test
    @DisplayName("Powinien natychmiast zatrzymać monitoring Live, gdy listener zgłosi żądanie stopu")
    void shouldStopLiveMonitoringImmediatelyWhenRequested() throws Exception {
        // Given
        when(mockClient.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(500));

        LiveMonitorListener mockLiveListener = Mockito.mock(LiveMonitorListener.class);
        when(mockLiveListener.shouldStop()).thenReturn(true);

        // When
        dataService.monitorRealTime(mockLiveListener);

        // Then
        verify(mockLiveListener).onMonitorStart();
        verify(mockLiveListener).onMonitorStopped();
        verify(mockLiveListener, never()).onNewBlockProcessed(any());
    }
    @Test
    @DisplayName("Powinien przetworzyć i zapisać nowy blok wykryty podczas monitoringu Real-time")
    void shouldProcessNewBlockDuringLiveMonitoring() throws Exception {
        when(mockClient.getLatestBlockNumber())
                .thenReturn(BigInteger.valueOf(500))
                .thenReturn(BigInteger.valueOf(501));

        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1f5"); // 501 w Hex
        rawBlock.setHash("0xlivehash");
        rawBlock.setBaseFeePerGas("0x0");
        rawBlock.setTransactions(Collections.emptyList());
        when(mockClient.getBlockDetails(BigInteger.valueOf(501))).thenReturn(rawBlock);

        LiveMonitorListener mockLiveListener = Mockito.mock(LiveMonitorListener.class);

        when(mockLiveListener.shouldStop()).thenReturn(false, true);

        // When
        dataService.monitorRealTime(mockLiveListener);

        // Then
        verify(mockLiveListener).onMonitorStart();
        verify(mockLiveListener).onNewBlockProcessed(any(BlockDTO.class));
        verify(mockLiveListener).onMonitorStopped();
        verify(mockStats).recordBlock(0);
    }
}