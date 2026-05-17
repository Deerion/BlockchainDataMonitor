package pl.skompilowani.service.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import pl.skompilowani.infrastructure.mapper.BlockchainMapper;
import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.core.model.TransactionDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BlockchainMapper - Testy mapowania danych")
class BlockchainMapperTest {

    @Test
    @DisplayName("Powinien poprawnie zmapować pełny EthBlock.Block i listę DTO transakcji")
    void shouldMapEthBlockToBlockDTOWithTransactions() {
        // Given
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x10"); // 16 w dziesiętnym
        rawBlock.setHash("0xabcdef1234567890");
        rawBlock.setBaseFeePerGas("0x3b9aca00"); // 1000000000

        // Symulujemy, że blok wewnątrz ma 2 transakcje (żeby sprawdzić czy dobrze liczy txCount)
        EthBlock.TransactionResult<Transaction> txResult1 = new EthBlock.TransactionObject();
        EthBlock.TransactionResult<Transaction> txResult2 = new EthBlock.TransactionObject();
        rawBlock.setTransactions(Arrays.asList(txResult1, txResult2));

        // Przygotowujemy sztuczną listę zmapowanych transakcji DTO
        List<TransactionDTO> txDtos = Arrays.asList(
                new TransactionDTO("0xhash1", "0xfrom1", "0xto1", BigDecimal.ONE, BigDecimal.ZERO, 21000L, BigInteger.TEN),
                new TransactionDTO("0xhash2", "0xfrom2", "0xto2", BigDecimal.TEN, BigDecimal.ZERO, 21000L, BigInteger.TEN)
        );

        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock, txDtos);

        assertNotNull(result);
        assertEquals(BigInteger.valueOf(16), result.number());
        assertEquals("0xabcdef1234567890", result.hash());
        assertEquals(2, result.transactionCount(), "Powinien pobrać rozmiar oryginalnej listy transakcji z bloku");
        assertEquals(new BigInteger("1000000000"), result.baseFeePerGas());
        assertEquals(2, result.transactions().size(), "Zmapowana lista DTO powinna zostać przypisana");
    }

    @Test
    @DisplayName("Powinien poprawnie zmapować blok przy użyciu przeciążonej metody (bez listy transakcji)")
    void shouldMapEthBlockToBlockDTOWithoutTransactionsList() {
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0xabcdef1234567890");
        rawBlock.setBaseFeePerGas("0x3b9aca00");
        rawBlock.setTransactions(Collections.emptyList());

        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock);

        assertNotNull(result);
        assertTrue(result.transactions().isEmpty(), "Domyślna lista transakcji powinna być pusta");
        assertEquals(0, result.transactionCount());
    }

    @Test
    @DisplayName("Powinien zwrócić null, gdy podany blok jest nullem")
    void shouldReturnNullWhenBlockIsNull() {
        assertNull(BlockchainMapper.toBlockDTO(null));
        assertNull(BlockchainMapper.toBlockDTO(null, Collections.emptyList()));
    }

    @Test
    @DisplayName("Powinien ustawić transactionCount na 0, gdy w bloku lista transakcji jest nullem")
    void shouldHandleNullTransactionsInsideRawBlock() {
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0x123456");
        rawBlock.setBaseFeePerGas("0x0");
        rawBlock.setTransactions(null); // Brak transakcji z sieci

        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock);

        assertNotNull(result);
        assertEquals(0, result.transactionCount());
        assertNotNull(result.transactions(), "Lista DTO nie powinna być nullem, lecz pustą listą");
    }

    @Test
    @DisplayName("Powinien ustawić pustą listę transakcji DTO, gdy przekazano null zamiast listy")
    void shouldHandleNullTransactionDTOListPassedAsArgument() {
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0x123456");
        rawBlock.setBaseFeePerGas("0x0");

        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock, null);

        assertNotNull(result);
        assertNotNull(result.transactions());
        assertTrue(result.transactions().isEmpty());
    }

    @Test
    @DisplayName("Powinien poprawnie zmapować Transaction na TransactionDTO z konwersją Wei na Ether")
    void shouldMapTransactionToTransactionDTO() {
        Transaction tx = new Transaction();
        tx.setHash("0x1234567890abcdef");
        tx.setFrom("0x1111111111111111111111111111111111111111");
        tx.setTo("0x2222222222222222222222222222222222222222");
        // "0xde0b6b3a7640000" == 1 000 000 000 000 000 000 Wei == 1 Ether
        tx.setValue("0xde0b6b3a7640000");

        long expectedGasUsed = 21000L;
        BigInteger expectedTimestamp = BigInteger.valueOf(1672531200L);
        BigDecimal expectedOplataEth = new BigDecimal("0.005");

        TransactionDTO result = BlockchainMapper.toTransactionDTO(tx, expectedGasUsed, expectedTimestamp, expectedOplataEth);

        assertNotNull(result);
        assertEquals("0x1234567890abcdef", result.hash());
        assertEquals("0x1111111111111111111111111111111111111111", result.from());
        assertEquals("0x2222222222222222222222222222222222222222", result.to());
        assertEquals(new BigDecimal("1"), result.valueEth(), "Wartość w Wei powinna zostać poprawnie zamieniona na Ether");
        assertEquals(expectedOplataEth, result.oplataEth());
        assertEquals(expectedGasUsed, result.gasUsed());
        assertEquals(expectedTimestamp, result.timestamp());
    }

    @Test
    @DisplayName("Powinien użyć BigDecimal.ZERO dla oplataEth przy korzystaniu z metody trójargumentowej")
    void shouldMapTransactionWithDefaultZeroFee() {
        Transaction tx = new Transaction();
        tx.setHash("0x1234567890abcdef");
        tx.setFrom("0x1111111111111111111111111111111111111111");
        tx.setTo("0x2222222222222222222222222222222222222222");
        tx.setValue("0x0"); // 0 Ether

        TransactionDTO result = BlockchainMapper.toTransactionDTO(tx, 21000L, BigInteger.TEN);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.oplataEth(), "Domyślna oplataEth powinna wynosić 0");
    }

    @Test
    @DisplayName("Powinien fallbackować oplataEth do BigDecimal.ZERO, gdy podano null w pełnej metodzie")
    void shouldFallbackToZeroFeeWhenNullPassed() {
        Transaction tx = new Transaction();
        tx.setHash("0x1234567890abcdef");
        tx.setFrom("0x1111111111111111111111111111111111111111");
        tx.setTo("0x2222222222222222222222222222222222222222");
        tx.setValue("0x0");

        TransactionDTO result = BlockchainMapper.toTransactionDTO(tx, 21000L, BigInteger.TEN, null);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.oplataEth());
    }

    @Test
    @DisplayName("Powinien zwrócić null, gdy podana transakcja jest nullem")
    void shouldReturnNullWhenTransactionIsNull() {
        assertNull(BlockchainMapper.toTransactionDTO(null, 0L, null));
        assertNull(BlockchainMapper.toTransactionDTO(null, 0L, null, BigDecimal.ONE));
    }
}