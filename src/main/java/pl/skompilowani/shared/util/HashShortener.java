package pl.skompilowani.shared.util;

public class HashShortener {

    public static String shorten(String hash) {
        if (hash == null || hash.length() <= 10) {
            return hash;
        }

        String prefix = hash.substring(0, 6);

        String suffix = hash.substring(hash.length() - 4);

        return prefix + "..." + suffix;
    }
}