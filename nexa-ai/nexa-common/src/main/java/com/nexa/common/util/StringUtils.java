package com.nexa.common.util;

public final class StringUtils {
    private StringUtils() {}

    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    public static String sanitize(String str) {
        if (str == null) return null;
        return str.trim().replaceAll("[<>\"'&]", "");
    }

    public static int estimateTokenCount(String text) {
        if (text == null) return 0;
        // Rough estimate: 1 token ≈ 4 chars
        return (int) Math.ceil(text.length() / 4.0);
    }
}
