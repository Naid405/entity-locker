package iasemenov.util;

public final class Assert {
    private Assert() {
    }

    public static void notNull(Object toValidate, String message) {
        if (toValidate == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void moreTheZero(int toValidate, String message) {
        if (toValidate <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
