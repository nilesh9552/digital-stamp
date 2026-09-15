package com.digitalstamp.util;

import java.security.SecureRandom;
import java.util.UUID;

public final class TokenUtil {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private TokenUtil() {
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String redemptionCode() {
        StringBuilder sb = new StringBuilder("RWD-");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHANUM[RANDOM.nextInt(ALPHANUM.length)]);
        }
        return sb.toString();
    }
}
