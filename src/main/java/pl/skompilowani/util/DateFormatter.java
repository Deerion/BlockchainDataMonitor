package pl.skompilowani.util;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    // Tworzymy formater, który zdefiniuje wygląd daty, np. 2026-05-04 21:03:23
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault()); // Używamy strefy czasowej użytkownika (np. CEST)

    public static String format(BigInteger unixTimestamp) {
        if (unixTimestamp == null) {
            return "Brak daty";
        }

        // Zamieniamy BigInteger na długą liczbę całkowitą (long)
        long seconds = unixTimestamp.longValue();

        // Tworzymy obiekt Instant z sekund i formatujemy do Stringa
        return FORMATTER.format(Instant.ofEpochSecond(seconds));
    }
}