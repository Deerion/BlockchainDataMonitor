package pl.skompilowani.service.dto;

import java.math.BigDecimal;

public record TransactionDTO(
        String hash,
        String from,
        String to,
        BigDecimal valueEth,
        long gasUsed
) {}