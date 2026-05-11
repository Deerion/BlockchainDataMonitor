package pl.skompilowani.util;

import java.util.regex.Pattern;

public final class AddressValidator {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^0x[0-9a-fA-F]{40}$");

    private AddressValidator() {}

    public static boolean isValid(String address) {
        return address != null && ADDRESS_PATTERN.matcher(address).matches();
    }

    public static String normalize(String address) {
        if (!isValid(address)) {
            throw new IllegalArgumentException("Nieprawidłowy adres Ethereum: " + address);
        }
        return address.toLowerCase();
    }
}
