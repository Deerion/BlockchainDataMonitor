package pl.skompilowani.shared.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * Klasa narzędziowa do konwersji jednostek Ethereum.
 */
public final class UnitConverter {

    private static final BigDecimal WEI_IN_GWEI = new BigDecimal("1000000000"); // 10^9
    private static final BigDecimal WEI_IN_ETHER = new BigDecimal("1000000000000000000"); // 10^18

    private UnitConverter() {
        // Prywatny konstruktor, aby zapobiec tworzeniu instancji
    }

    /**
     * Konwertuje wartość z Wei na Gwei.
     *
     * @param wei Wartość w Wei.
     * @return Wartość w Gwei.
     */
    public static BigDecimal weiToGwei(BigInteger wei) {
        if (wei == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wei).divide(WEI_IN_GWEI, 9, RoundingMode.HALF_UP);
    }

    /**
     * Konwertuje wartość z Wei na Gwei.
     *
     * @param wei Wartość w Wei.
     * @return Wartość w Gwei.
     */
    public static BigDecimal weiToGwei(BigDecimal wei) {
        if (wei == null) {
            return BigDecimal.ZERO;
        }
        return wei.divide(WEI_IN_GWEI, 9, RoundingMode.HALF_UP);
    }

    /**
     * Konwertuje wartość z Wei na Ether.
     *
     * @param wei Wartość w Wei.
     * @return Wartość w Ether.
     */
    public static BigDecimal weiToEther(BigInteger wei) {
        if (wei == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(wei).divide(WEI_IN_ETHER, 18, RoundingMode.HALF_UP);
    }
}


