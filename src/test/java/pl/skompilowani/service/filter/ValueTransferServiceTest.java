package pl.skompilowani.service.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import pl.skompilowani.api.BlockchainClient;
import pl.skompilowani.service.dto.AddressTransferDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("ValueTransferService - Testy filtrowania transakcji")
class ValueTransferServiceTest {

    private BlockchainClient blockchainClientMock;
    private ValueTransferService valueTransferService;

    @BeforeEach
    void setUp() {
        // Izolujemy serwis od warstwy dostępu (API) za pomocą mocka
        blockchainClientMock = Mockito.mock(BlockchainClient.class);
        valueTransferService = new ValueTransferService(blockchainClientMock);
    }

    @Test
    @DisplayName("Powinien odrzucić transakcje poniżej minimalnej wartości ETH")
    void shouldFilterOutTransactionsBelowMinValue() throws Exception {
        BigInteger latestBlockNum = BigInteger.valueOf(100);
        when(blockchainClientMock.getLatestBlockNumber()).thenReturn(latestBlockNum);

        // 0.5 ETH w HEX (Wei) == 0x6f05b59d3b20000
        EthBlock.TransactionObject txLow = createMockTx("0xhash_low", "0x6f05b59d3b20000");
        // 1.0 ETH w HEX (Wei) == 0xde0b6b3a7640000
        EthBlock.TransactionObject txExact = createMockTx("0xhash_exact", "0xde0b6b3a7640000");
        // 2.5 ETH w HEX (Wei) == 0x22b1c8c1227a0000
        EthBlock.TransactionObject txHigh = createMockTx("0xhash_high", "0x22b1c8c1227a0000");

        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x64"); // Blok 100
        mockBlock.setTimestamp("0x66440000"); // Dowolny timestamp
        mockBlock.setTransactions(Arrays.asList(txLow, txExact, txHigh));

        // Zwracamy ten blok tylko dla pierwszego zapytania, dla starszych zwracamy pusty, żeby szybciej skończyć pętlę
        when(blockchainClientMock.getBlockDetails(latestBlockNum)).thenReturn(mockBlock);
        when(blockchainClientMock.getBlockDetails(argThat(num -> num.compareTo(latestBlockNum) < 0)))
                .thenReturn(new EthBlock.Block());

        // Szukamy transakcji o wartości co najmniej 1.0 ETH
        List<AddressTransferDTO> result = valueTransferService.findLatestAbove(new BigDecimal("1.0"), 10);

        assertEquals(2, result.size(), "Transakcja 0.5 ETH powinna zostać odrzucona przez filtr");
        assertTrue(result.stream().anyMatch(tx -> tx.hash().equals("0xhash_exact")));
        assertTrue(result.stream().anyMatch(tx -> tx.hash().equals("0xhash_high")));
        assertFalse(result.stream().anyMatch(tx -> tx.hash().equals("0xhash_low")), "Odfiltrowana transakcja nie może znaleźć się w wynikach");
    }

    @Test
    @DisplayName("Powinien respektować maksymalny limit zwracanych transakcji")
    void shouldRespectResultLimit() throws Exception {
        BigInteger latestBlockNum = BigInteger.valueOf(100);
        when(blockchainClientMock.getLatestBlockNumber()).thenReturn(latestBlockNum);

        // Tworzymy blok z 5 poprawnymi transakcjami (wszystkie mają 2 ETH)
        EthBlock.TransactionObject tx1 = createMockTx("0x1", "0x1bc16d674ec80000");
        EthBlock.TransactionObject tx2 = createMockTx("0x2", "0x1bc16d674ec80000");
        EthBlock.TransactionObject tx3 = createMockTx("0x3", "0x1bc16d674ec80000");
        EthBlock.TransactionObject tx4 = createMockTx("0x4", "0x1bc16d674ec80000");
        EthBlock.TransactionObject tx5 = createMockTx("0x5", "0x1bc16d674ec80000");

        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x64");
        mockBlock.setTimestamp("0x66440000");
        mockBlock.setTransactions(Arrays.asList(tx1, tx2, tx3, tx4, tx5));

        when(blockchainClientMock.getBlockDetails(any(BigInteger.class))).thenReturn(mockBlock);

        int limit = 3;
        List<AddressTransferDTO> result = valueTransferService.findLatestAbove(BigDecimal.ONE, limit);

        assertEquals(limit, result.size(), "Serwis musi uciąć wyniki dokładnie do żądanego limitu");
    }

    @Test
    @DisplayName("Powinien poprawnie wzbogacać odfiltrowane transakcje o faktyczne opłaty za gaz")
    void shouldEnrichTransactionsWithGasFees() throws Exception {
        BigInteger latestBlockNum = BigInteger.valueOf(100);
        when(blockchainClientMock.getLatestBlockNumber()).thenReturn(latestBlockNum);

        EthBlock.TransactionObject tx = createMockTx("0xtarget", "0xde0b6b3a7640000"); // 1 ETH
        EthBlock.Block mockBlock = new EthBlock.Block();
        mockBlock.setNumber("0x64");
        mockBlock.setTimestamp("0x66440000");
        mockBlock.setTransactions(Collections.singletonList(tx));

        when(blockchainClientMock.getBlockDetails(latestBlockNum)).thenReturn(mockBlock);
        when(blockchainClientMock.getBlockDetails(argThat(num -> num.compareTo(latestBlockNum) < 0)))
                .thenReturn(new EthBlock.Block());

        // Mockujemy paragon transakcji, by zasymulować użycie gazu (gasUsed * effectiveGasPrice)
        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setGasUsed("0x5208"); // 21000 gazu
        receipt.setEffectiveGasPrice("0x3b9aca00"); // 1 Gwei (1000000000 Wei)
        when(blockchainClientMock.getTransactionReceipt("0xtarget")).thenReturn(Optional.of(receipt));

        List<AddressTransferDTO> result = valueTransferService.findLatestAbove(BigDecimal.ONE, 10);

        assertEquals(1, result.size());
        AddressTransferDTO enrichedTx = result.get(0);

        // 21000 * 1 Gwei == 21000 Gwei == 0.000021 ETH
        assertEquals(BigInteger.valueOf(21000), enrichedTx.gasUsed(), "Zużycie gazu powinno zostać zaciągnięte z receipta");
        assertEquals(new BigDecimal("0.000021000000000000"), enrichedTx.gasFeeEth(), "Opłata w ETH powinna być poprawnie przeliczona");
    }

    @Test
    @DisplayName("Powinien obsłużyć sytuację braku transakcji w skanowanych blokach")
    void shouldReturnEmptyListWhenNoTransactionsFound() throws Exception {
        when(blockchainClientMock.getLatestBlockNumber()).thenReturn(BigInteger.valueOf(100));
        // Zwracamy pusty blok za każdym razem
        when(blockchainClientMock.getBlockDetails(any())).thenReturn(new EthBlock.Block());

        List<AddressTransferDTO> result = valueTransferService.findLatestAbove(BigDecimal.ONE, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty(), "Jeżeli w blokach nie ma transakcji spełniających warunek, zwracana jest pusta lista");
    }

    // --- Metody pomocnicze ---

    private EthBlock.TransactionObject createMockTx(String hash, String valueHexWei) {
        EthBlock.TransactionObject tx = new EthBlock.TransactionObject();
        tx.setHash(hash);
        tx.setFrom("0x1111111111111111111111111111111111111111");
        tx.setTo("0x2222222222222222222222222222222222222222");
        tx.setValue(valueHexWei);
        return tx;
    }
}