package pl.skompilowani.util;

public class HashShortener {

    public static String shorten(String hash) {
        // Zabezpieczenie przed błędami (gdyby coś przyszło jako null albo było dziwnie krótkie)
        if (hash == null || hash.length() <= 10) {
            return hash;
        }

        // Pobieramy 6 pierwszych znaków (np. "0x" + 4 znaki)
        String prefix = hash.substring(0, 6);

        // Pobieramy 4 ostatnie znaki
        String suffix = hash.substring(hash.length() - 4);

        // Sklejamy w całość
        return prefix + "..." + suffix;
    }
}