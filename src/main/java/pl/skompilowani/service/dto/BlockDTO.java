package pl.skompilowani.service.dto;

import java.math.BigInteger;

public record BlockDTO(
        java.math.BigInteger number,
        String hash,
        int transactionCount,
        java.math.BigInteger baseFeePerGas
) {}