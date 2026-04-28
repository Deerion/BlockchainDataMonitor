package pl.skompilowani.service.mapper;

import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainMapperTest {

    @Test
    void shouldMapEthBlockToBlockDTO() {
        // Given
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0xabc123");
        rawBlock.setBaseFeePerGas("0x3B9ACA00");
        rawBlock.setTransactions(Collections.emptyList());

        // When
        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock);

        // Then
        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.number());
        assertEquals("0xabc123", result.hash());
        assertEquals(0, result.transactionCount());
        assertEquals(new BigInteger("1000000000"), result.baseFeePerGas());
    }

    @Test
    void shouldMapTransactionToTransactionDTO() {
        // Given
        Transaction tx = new Transaction();
        tx.setHash("0xhash123");
        tx.setFrom("0xsender");
        tx.setTo("0xreceiver");
        tx.setValue("0xde0b6b3a7640000");
        tx.setGas("0x5208"); // 21000

        // When
        TransactionDTO result = BlockchainMapper.toTransactionDTO(tx);

        // Then
        assertNotNull(result);
        assertEquals("0xhash123", result.hash());
        assertEquals("0xsender", result.from());
        assertEquals("0xreceiver", result.to());
        assertEquals(new BigDecimal("1"), result.valueEth());
        assertEquals(21000L, result.gasUsed());
    }

    @Test
    void shouldHandleNullInputs() {
        assertNull(BlockchainMapper.toBlockDTO(null));
        assertNull(BlockchainMapper.toTransactionDTO(null));
    }

    @Test
    void shouldHandleNullTransactionsInBlock() {
        // Given
        EthBlock.Block rawBlock = new EthBlock.Block();
        rawBlock.setNumber("0x1");
        rawBlock.setHash("0x123");
        rawBlock.setBaseFeePerGas("0x0");
        rawBlock.setTransactions(null);

        // When
        BlockDTO result = BlockchainMapper.toBlockDTO(rawBlock);

        // Then
        assertNotNull(result);
        assertEquals(BigInteger.ONE, result.number());
        assertEquals(0, result.transactionCount());
        assertEquals(BigInteger.ZERO, result.baseFeePerGas());
    }
}