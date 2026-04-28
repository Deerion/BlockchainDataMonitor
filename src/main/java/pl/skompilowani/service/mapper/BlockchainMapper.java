package pl.skompilowani.service.mapper;

import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import pl.skompilowani.service.dto.BlockDTO;
import pl.skompilowani.service.dto.TransactionDTO;

import java.math.BigDecimal;

public class BlockchainMapper {

    public static BlockDTO toBlockDTO(EthBlock.Block block) {
        if (block == null) return null;

        int txCount = (block.getTransactions() != null) ? block.getTransactions().size() : 0;

        return new BlockDTO(
                block.getNumber(),
                block.getHash(),
                txCount, // Używamy bezpiecznej wartości
                block.getBaseFeePerGas()
        );
    }

    public static TransactionDTO toTransactionDTO(Transaction tx) {
        if (tx == null) return null;

        BigDecimal valueInEth = Convert.fromWei(tx.getValue().toString(), Convert.Unit.ETHER);

        return new TransactionDTO(
                tx.getHash(),
                tx.getFrom(),
                tx.getTo(),
                valueInEth,
                tx.getGas().longValue()
        );
    }
}