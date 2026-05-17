package pl.skompilowani.core.model;

import java.math.BigDecimal;
import java.math.BigInteger;

public record AddressTransferDTO(
        String hash,
        String from,
        String to,
        BigInteger blockNumber,
        BigDecimal value,
        String asset,
        String category,
        String blockTimestamp,
        BigInteger gasUsed,
        BigDecimal gasFeeEth
) {}
