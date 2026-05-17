package pl.skompilowani.core.service;

import pl.skompilowani.core.model.BlockDTO;
import pl.skompilowani.core.model.TransactionDTO;

public interface TransactionLogRepository {
    void logTransaction(BlockDTO block, TransactionDTO tx);
}