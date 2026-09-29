package com.example.demo.security.util;

/**
 * Tiện ích làm mờ (Masking) dữ liệu nhạy cảm để tránh rò rỉ qua API hoặc Log.
 */
public final class MaskingUtils {

    private MaskingUtils() {}

    /**
     * Làm mờ số điện thoại: 0987654321 -> 098****321
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return phone;
        }
        String trimmed = phone.trim();
        if (trimmed.length() <= 5) {
            return "***";
        }
        int keep = Math.min(3, trimmed.length() / 3);
        String prefix = trimmed.substring(0, keep);
        String suffix = trimmed.substring(trimmed.length() - keep);
        return prefix + "****" + suffix;
    }

    /**
     * Làm mờ email: username@domain.com -> u***e@domain.com
     */
    public static String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        String domain = parts[1];

        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.charAt(0) + "***" + name.charAt(name.length() - 1) + "@" + domain;
    }

    /**
     * Làm mờ số CCCD / thẻ ngân hàng: chỉ giữ 3 số đầu và 3 số cuối
     */
    public static String maskCardNumber(String card) {
        if (card == null || card.isBlank()) {
            return card;
        }
        String trimmed = card.trim();
        if (trimmed.length() <= 6) {
            return "******";
        }
        return trimmed.substring(0, 3) + "******" + trimmed.substring(trimmed.length() - 3);
    }
}
