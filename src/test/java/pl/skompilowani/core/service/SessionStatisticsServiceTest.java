package pl.skompilowani.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import pl.skompilowani.core.model.AddressTransferDTO;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SessionStatisticsService - Testy agregacji danych i polskiej gramatyki")
class SessionStatisticsServiceTest {

    private SessionStatisticsService statsService;

    @BeforeEach
    void setUp() {
        statsService = new SessionStatisticsService();
    }

    @Test
    @DisplayName("Powinien poprawnie rejestrować przetworzone bloki oraz sumować transakcje")
    void shouldCorrectlyRecordBlocksAndTransactions() {
        // When
        statsService.recordBlock(5);
        statsService.recordBlock(12);

        // Then
        assertEquals(2, statsService.getTotalBlocksProcessed());
        assertEquals(17, statsService.getTotalTransactionsProcessed());
    }

    @Test
    @DisplayName("Powinien wyłonić transakcję o najwyższej wartości i zsumować łączną wartość ETH")
    void shouldTrackMaxTransactionValueAndTotalEth() {
        // When
        statsService.recordTransactionValue(new BigDecimal("1.5"), "0xhashA");
        statsService.recordTransactionValue(new BigDecimal("5.2"), "0xhashMax");
        statsService.recordTransactionValue(new BigDecimal("0.1"), "0xhashC");

        // Then
        assertEquals(new BigDecimal("6.8"), statsService.getTotalValueEth());
        assertEquals(new BigDecimal("5.2"), statsService.getMaxTransactionValue());
        assertEquals("0xhashMax", statsService.getMaxTransactionHash());
    }

    @Test
    @DisplayName("Powinien prawidłowo przetwarzać przefiltrowane wyniki")
    void shouldRecordFilteredResultsValuesAndIncrements() {
        // Given
        AddressTransferDTO tx1 = new AddressTransferDTO("0x1", "0xfrom", "0xto", BigInteger.ONE, new BigDecimal("2.5"), "ETH", "ext", "ts", BigInteger.ZERO, BigDecimal.ZERO);
        AddressTransferDTO tx2 = new AddressTransferDTO("0x2", "0xfrom", "0xto", BigInteger.ONE, new BigDecimal("0.5"), "ETH", "ext", "ts", BigInteger.ZERO, BigDecimal.ZERO);

        // When
        statsService.recordFilteredResults(2, List.of(tx1, tx2));

        // Then
        assertEquals(2, statsService.getTotalTransactionsProcessed());
        assertEquals(new BigDecimal("3.0"), statsService.getTotalValueEth());
        assertEquals(new BigDecimal("2.5"), statsService.getMaxTransactionValue());
        assertEquals("0x1", statsService.getMaxTransactionHash());
    }

    @ParameterizedTest
    @CsvSource({
            "1, 1 blok | 1 transakcja",
            "2, 2 bloki | 2 transakcje",
            "5, 5 bloków | 5 transakcji",
            "12, 12 bloków | 12 transakcji",
            "22, 22 bloki | 22 transakcje",
            "104, 104 bloki | 104 transakcje",
            "112, 112 bloków | 112 transakcji",
            "0, 0 bloków | 0 transakcji"
    })
    @DisplayName("Test specyfiki polskiej fleksji językowej dla statystyk")
    void shouldReturnCorrectPolishPluralForms(int count, String expectedOutput) {
        // Given
        SessionStatisticsService customStats = new SessionStatisticsService();

        customStats.recordBlock(count);

        // Resetujemy i budujemy stan krok po kroku:
        SessionStatisticsService exactStats = new SessionStatisticsService();
        for(int i = 0; i < count; i++) {
            exactStats.recordBlock(1);
        }
        if (count == 0) {
            exactStats = new SessionStatisticsService();
        }

        // When
        String formatted = exactStats.getFormattedStats();

        // Then
        assertEquals(expectedOutput, formatted);
    }

    @Test
    @DisplayName("Powinien poprawnie sformatować czas trwania sesji")
    void shouldReturnValidSessionDurationPattern() {
        // When
        String duration = statsService.getSessionDuration();

        // Then
        // Sprawdzenie wzorca formatu zegara cyfrowego: HH:mm:ss
        assertTrue(duration.matches("^\\d{2}:\\d{2}:\\d{2}$"));
    }

    @Test
    @DisplayName("Powinien obsłużyć przestarzałą metodę zapisu wartości z domyślnym hashem")
    void shouldHandleDeprecatedRecordTransactionValue() {
        // When
        statsService.recordTransactionValue(BigDecimal.TEN);

        // Then
        assertEquals(BigDecimal.TEN, statsService.getTotalValueEth());
        assertEquals("Nieznany", statsService.getMaxTransactionHash());
    }
}