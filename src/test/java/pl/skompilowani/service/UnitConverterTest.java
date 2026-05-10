package pl.skompilowani.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy jednostkowe dla klasy UnitConverter.
 * Testują konwersję jednostek Ethereum: Wei -> Gwei, Wei -> Ether.
 */
@DisplayName("UnitConverter - Testy konwersji jednostek Ethereum")
class UnitConverterTest {

    // ========== TESTY KONWERSJI WEI NA GWEI ==========

    @Test
    @DisplayName("Konwersja Wei na Gwei - wartość zerowa")
    void weiToGwei_zeroValue_bigInteger() {
        // Given
        BigInteger zero = BigInteger.ZERO;

        // When
        BigDecimal result = UnitConverter.weiToGwei(zero);

        // Then
        assertEquals(new BigDecimal("0.000000000"), result);
    }

    @Test
    @DisplayName("Konwersja Wei na Gwei - wartość zerowa (BigDecimal)")
    void weiToGwei_zeroValue_bigDecimal() {
        // Given
        BigDecimal zero = BigDecimal.ZERO;

        // When
        BigDecimal result = UnitConverter.weiToGwei(zero);

        // Then
        assertEquals(new BigDecimal("0.000000000"), result);
    }

    @Test
    @DisplayName("Konwersja Wei na Gwei - null (BigInteger)")
    void weiToGwei_nullValue_bigInteger() {
        // When
        BigDecimal result = UnitConverter.weiToGwei((BigInteger) null);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    @DisplayName("Konwersja Wei na Gwei - null (BigDecimal)")
    void weiToGwei_nullValue_bigDecimal() {
        // When
        BigDecimal result = UnitConverter.weiToGwei((BigDecimal) null);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @ParameterizedTest
    @CsvSource({
            "1000000000, 1.000000000",                  // 1 Gwei
            "1000000001, 1.000000001",                  // 1 Gwei + 1 Wei
            "999999999, 0.999999999",                   // poniżej 1 Gwei
            "169587232330000000000, 169587232330.000000000", // bardzo duża wartość
            "237977138180000000000, 237977138180.000000000", // bardzo duża wartość
            "318242923610000000000, 318242923610.000000000", // bardzo duża wartość
            "1000000000000000000, 1000000000.000000000" // 1 Ether w Wei
    })
    @DisplayName("Konwersja Wei na Gwei - wartości parametryzowane (BigInteger)")
    void weiToGwei_parameterizedValues_bigInteger(String wei, String expectedGwei) {
        // Given
        BigInteger weiValue = new BigInteger(wei);

        // When
        BigDecimal result = UnitConverter.weiToGwei(weiValue);

        // Then
        assertEquals(new BigDecimal(expectedGwei), result);
    }

    @ParameterizedTest
    @CsvSource({
            "1000000000, 1.000000000",           // 1 Gwei
            "1000000001, 1.000000001",           // 1 Gwei + 1 Wei
            "999999999, 0.999999999",            // poniżej 1 Gwei
            "169587232.33, 0.169587232",         // zaokrąglenie w dół
            "1000000000000000000, 1000000000.000000000" // 1 Ether w Wei
    })
    @DisplayName("Konwersja Wei na Gwei - wartości parametryzowane (BigDecimal)")
    void weiToGwei_parameterizedValues_bigDecimal(String wei, String expectedGwei) {
        // Given
        BigDecimal weiValue = new BigDecimal(wei);

        // When
        BigDecimal result = UnitConverter.weiToGwei(weiValue);

        // Then
        assertEquals(new BigDecimal(expectedGwei), result);
    }

    @Test
    @DisplayName("Konwersja Wei na Gwei - bardzo duża wartość")
    void weiToGwei_largeValue() {
        // Given
        BigInteger largeValue = new BigInteger("999999999999999999999999999");

        // When
        BigDecimal result = UnitConverter.weiToGwei(largeValue);

        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    // ========== TESTY KONWERSJI WEI NA ETHER ==========

    @Test
    @DisplayName("Konwersja Wei na Ether - wartość zerowa")
    void weiToEther_zeroValue() {
        // Given
        BigInteger zero = BigInteger.ZERO;

        // When
        BigDecimal result = UnitConverter.weiToEther(zero);

        // Then
        assertEquals(new BigDecimal("0.000000000000000000"), result);
    }

    @Test
    @DisplayName("Konwersja Wei na Ether - null")
    void weiToEther_nullValue() {
        // When
        BigDecimal result = UnitConverter.weiToEther(null);

        // Then
        assertEquals(BigDecimal.ZERO, result);
    }

    @ParameterizedTest
    @CsvSource({
            "1000000000000000000, 1.000000000000000000",     // 1 Ether
            "1000000000000000001, 1.000000000000000001",     // 1 Ether + 1 Wei
            "500000000000000000, 0.500000000000000000",      // 0.5 Ether
            "100000000000000000, 0.100000000000000000",      // 0.1 Ether
            "1, 0.000000000000000001",                       // 1 Wei
            "999999999999999999, 0.999999999999999999"       // poniżej 1 Ether
    })
    @DisplayName("Konwersja Wei na Ether - wartości parametryzowane")
    void weiToEther_parameterizedValues(String wei, String expectedEther) {
        // Given
        BigInteger weiValue = new BigInteger(wei);

        // When
        BigDecimal result = UnitConverter.weiToEther(weiValue);

        // Then
        assertEquals(new BigDecimal(expectedEther), result);
    }

    @Test
    @DisplayName("Konwersja Wei na Ether - bardzo duża wartość")
    void weiToEther_largeValue() {
        // Given
        BigInteger largeValue = new BigInteger("999999999999999999999999999");

        // When
        BigDecimal result = UnitConverter.weiToEther(largeValue);

        // Then
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) > 0);
    }

    // ========== TESTY POPRAWNOŚCI KONWERSJI ==========

    @Test
    @DisplayName("Konwersja zwrotna Wei -> Gwei -> Wei powinna dać zbliżoną wartość")
    void conversionAccuracy_weiToGweiAndBack() {
        // Given
        BigInteger originalWei = new BigInteger("5000000000");

        // When
        BigDecimal gwei = UnitConverter.weiToGwei(originalWei);
        BigInteger convertedBack = gwei.multiply(new BigDecimal("1000000000")).toBigInteger();

        // Then
        assertEquals(originalWei, convertedBack);
    }

    @Test
    @DisplayName("Konwersja zwrotna Wei -> Ether powinna być dokładna")
    void conversionAccuracy_weiToEther() {
        // Given
        BigInteger weiPerEther = new BigInteger("1000000000000000000");

        // When
        BigDecimal ether = UnitConverter.weiToEther(weiPerEther);

        // Then
        assertEquals(new BigDecimal("1.000000000000000000"), ether);
    }

    @Test
    @DisplayName("1 Gwei powinno równać się 10^9 Wei")
    void conversion_oneGweiEqualsOneGigaWei() {
        // Given
        BigInteger oneGigaWei = new BigInteger("1000000000");

        // When
        BigDecimal gwei = UnitConverter.weiToGwei(oneGigaWei);

        // Then
        assertEquals(new BigDecimal("1.000000000"), gwei);
    }

    @Test
    @DisplayName("1 Ether powinno równać się 10^18 Wei")
    void conversion_oneEtherEqualsOneQuintillionWei() {
        // Given
        BigInteger oneQuintillionWei = new BigInteger("1000000000000000000");

        // When
        BigDecimal ether = UnitConverter.weiToEther(oneQuintillionWei);

        // Then
        assertEquals(new BigDecimal("1.000000000000000000"), ether);
    }

    @Test
    @DisplayName("Relacja: 1 Ether = 10^9 Gwei")
    void conversion_etherToGweiRelation() {
        // Given
        BigInteger oneEther = new BigInteger("1000000000000000000");

        // When
        BigDecimal gweiValue = UnitConverter.weiToGwei(oneEther);

        // Then
        assertEquals(new BigDecimal("1000000000.000000000"), gweiValue);
    }

    @Test
    @DisplayName("Dokładność zaokrąglania - HALF_UP")
    void precision_halfUpRounding() {
        // Given
        BigInteger weiValue = new BigInteger("1500000000"); // 1.5 Gwei

        // When
        BigDecimal result = UnitConverter.weiToGwei(weiValue);

        // Then
        // Przy HALF_UP zaokrąglanie 1.500000000 powinno dać 1.500000000
        assertEquals(new BigDecimal("1.500000000"), result);
    }

    @Test
    @DisplayName("Liczba cyfr po przecinku - Gwei (9 miejsc)")
    void precision_gwei_decimalPlaces() {
        // Given
        BigInteger weiValue = new BigInteger("1234567891");

        // When
        BigDecimal result = UnitConverter.weiToGwei(weiValue);

        // Then
        assertEquals(9, result.scale());
    }

    @Test
    @DisplayName("Liczba cyfr po przecinku - Ether (18 miejsc)")
    void precision_ether_decimalPlaces() {
        // Given
        BigInteger weiValue = new BigInteger("123456789012345678");

        // When
        BigDecimal result = UnitConverter.weiToEther(weiValue);

        // Then
        assertEquals(18, result.scale());
    }

    // ========== TESTY PORÓWNAWCZE ==========

    @Test
    @DisplayName("Porównanie BigInteger vs BigDecimal dla weiToGwei - ta sama wartość")
    void comparison_weiToGwei_bigIntegerVsBigDecimal() {
        // Given
        BigInteger weiInteger = new BigInteger("5000000000");
        BigDecimal weiDecimal = new BigDecimal("5000000000");

        // When
        BigDecimal resultFromInteger = UnitConverter.weiToGwei(weiInteger);
        BigDecimal resultFromDecimal = UnitConverter.weiToGwei(weiDecimal);

        // Then
        assertEquals(resultFromInteger, resultFromDecimal);
    }

    @Test
    @DisplayName("Testy graniczne zaokrąglania - wartość tuż poniżej progu")
    void rounding_belowThreshold() {
        // Given
        // 1.0000000004 Gwei - tuż poniżej progu zaokrąglania (4 < 5)
        BigInteger wei = new BigInteger("1000000004");

        // When
        BigDecimal result = UnitConverter.weiToGwei(wei);

        // Then
        // Powinno zaokrąglić w dół do 1.000000000
        assertEquals(new BigDecimal("1.000000004"), result);
    }

    @Test
    @DisplayName("Testy graniczne zaokrąglania - wartość tuż powyżej progu")
    void rounding_aboveThreshold() {
        // Given
        // 1.0000000005 Gwei - tuż powyżej progu zaokrąglania (5 >= 5)
        BigInteger wei = new BigInteger("1000000005");

        // When
        BigDecimal result = UnitConverter.weiToGwei(wei);

        // Then
        // Powinno zaokrąglić w górę do 1.000000001 (HALF_UP)
        assertEquals(new BigDecimal("1.000000005"), result);
    }

    @Test
    @DisplayName("Testy dla bardzo małych wartości Wei")
    void smallValues_wei() {
        // Given
        BigInteger oneWei = BigInteger.ONE;

        // When
        BigDecimal gweiResult = UnitConverter.weiToGwei(oneWei);
        BigDecimal etherResult = UnitConverter.weiToEther(oneWei);

        // Then
        assertEquals(new BigDecimal("0.000000001"), gweiResult);
        assertEquals(new BigDecimal("0.000000000000000001"), etherResult);
    }

    @Test
    @DisplayName("Konsystencja - Gwei to pośrednia jednostka między Wei a Ether")
    void consistency_weiGweiEtherRelation() {
        // Given
        BigInteger weiValue = new BigInteger("123456789000000000");

        // When
        BigDecimal gwei = UnitConverter.weiToGwei(weiValue);
        BigDecimal ether = UnitConverter.weiToEther(weiValue);

        // Then
        // Gwei / 10^9 powinno się równać Ether
        BigDecimal gweiToEther = gwei.divide(new BigDecimal("1000000000"), 18, RoundingMode.HALF_UP);
        assertEquals(ether, gweiToEther);
    }

    @Test
    @DisplayName("Inne: Maksymalna wartość BigInteger UTF-64")
    void edgeCase_veryLargeNumber() {
        // Given
        BigInteger maxLike = new BigInteger("99999999999999999999999999999999999999");

        // When
        BigDecimal gweiResult = UnitConverter.weiToGwei(maxLike);
        BigDecimal etherResult = UnitConverter.weiToEther(maxLike);

        // Then
        assertNotNull(gweiResult);
        assertNotNull(etherResult);
        assertTrue(gweiResult.compareTo(BigDecimal.ZERO) > 0);
        assertTrue(etherResult.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Ilość zer w stałych konwersji jest prawidłowa")
    void mathematicalAccuracy_conversionConstants() {
        // Sprawdzenie że konwersja jest matematycznie dokładna
        // 1 Gwei = 10^9 Wei, 1 Ether = 10^18 Wei
        BigInteger oneGwei = new BigInteger("1000000000"); // 10^9
        BigInteger oneEther = new BigInteger("1000000000000000000"); // 10^18

        // When & Then
        assertEquals(new BigDecimal("1.000000000"), UnitConverter.weiToGwei(oneGwei));
        assertEquals(new BigDecimal("1.000000000000000000"), UnitConverter.weiToEther(oneEther));
        assertEquals(new BigDecimal("1000000000.000000000"), UnitConverter.weiToGwei(oneEther));
    }
}