package com.digitalstamp.util;

public final class QrUtil {
    public static final String PREFIX = "CUSTOMER:";

    private QrUtil() {
    }

    public static String payload(String qrToken) {
        return PREFIX + qrToken;
    }

    public static String parseToken(String payload) {
        if (payload == null) {
            return null;
        }
        String trimmed = payload.trim();
        if (trimmed.startsWith(PREFIX)) {
            return trimmed.substring(PREFIX.length()).trim();
        }
        return trimmed;
    }
}
