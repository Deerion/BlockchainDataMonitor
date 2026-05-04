package pl.skompilowani.service.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public record TransactionDTO(
        String hash,
        String from,
        String to,
        BigDecimal valueEth,
        long gasUsed,
        BigInteger timestamp
) {}