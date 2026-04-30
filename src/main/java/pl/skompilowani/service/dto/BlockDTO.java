package pl.skompilowani.service.dto;

import java.math.BigInteger;
import java.util.List;

public record BlockDTO(
        BigInteger number,
        String hash,
        int transactionCount,
        BigInteger baseFeePerGas,
        List<TransactionDTO> transactions
) {}