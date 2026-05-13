package pl.skompilowani.service;

import pl.skompilowani.service.dto.AddressTransferDTO;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class SessionStatisticsService {
    private int totalBlocksProcessed = 0;
    private int totalTransactionsProcessed = 0;
    private BigDecimal totalValueEth = BigDecimal.ZERO;
    private final Instant startTime = Instant.now();

    public synchronized void recordBlock(int txCount) {
        this.totalBlocksProcessed++;
        this.totalTransactionsProcessed += txCount;
    }

    public synchronized void recordTransactionValue(BigDecimal value) {
        if (value != null) this.totalValueEth = this.totalValueEth.add(value);
    }

    public int getTotalBlocksProcessed() { return totalBlocksProcessed; }
    public int getTotalTransactionsProcessed() { return totalTransactionsProcessed; }
    public BigDecimal getTotalValueEth() { return totalValueEth; }

    /**
     * Zwraca czas trwania sesji w formacie cyfrowego zegara HH:mm:ss.
     */
    public String getSessionDuration() {
        Duration d = Duration.between(startTime, Instant.now());
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    /**
     * Helper do poprawnej polskiej odmiany słów "blok" i "transakcja".
     */
    public String getFormattedStats() {
        return String.format("%d %s | %d %s",
                totalBlocksProcessed, getPolishPlural(totalBlocksProcessed, "blok", "bloki", "bloków"),
                totalTransactionsProcessed, getPolishPlural(totalTransactionsProcessed, "transakcja", "transakcje", "transakcji"));
    }

    private String getPolishPlural(int n, String s1, String s2, String s5) {
        if (n == 1) return s1;
        if (n % 10 >= 2 && n % 10 <= 4 && (n % 100 < 10 || n % 100 >= 20)) return s2;
        return s5;
    }

    public synchronized void recordFilteredResults(int count, java.util.List<pl.skompilowani.service.dto.AddressTransferDTO> results) {
        this.totalTransactionsProcessed += count;
        if (results != null) {
            for (pl.skompilowani.service.dto.AddressTransferDTO tx : results) {
                // W rekordach używamy nazwy pola jako metody: .value() zamiast .getValue()
                if (tx.value() != null) {
                    this.totalValueEth = this.totalValueEth.add(tx.value());
                }
            }
        }
    }
}