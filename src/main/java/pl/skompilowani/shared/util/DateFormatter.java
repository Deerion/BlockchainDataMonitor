package pl.skompilowani.shared.util;

import java.math.BigInteger;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public static String format(BigInteger unixTimestamp) {
        if (unixTimestamp == null) {
            return "Brak daty";
        }

        long seconds = unixTimestamp.longValue();

        return FORMATTER.format(Instant.ofEpochSecond(seconds));
    }
}