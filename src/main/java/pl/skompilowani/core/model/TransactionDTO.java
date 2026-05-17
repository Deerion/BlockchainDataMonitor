package pl.skompilowani.core.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public record TransactionDTO(
        String hash,
        String from,
        String to,
        BigDecimal valueEth,
        BigDecimal oplataEth,
        long gasUsed,
        BigInteger timestamp
) {}