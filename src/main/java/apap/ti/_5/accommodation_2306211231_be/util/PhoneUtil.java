package apap.ti._5.accommodation_2306211231_be.util;

public final class PhoneUtil {
    private PhoneUtil() {}

    // Normalize phone number to canonical formats or throw IllegalArgumentException
    // Allowed inputs:
    // - Indonesian mobile starting with 08 followed by 8-14 digits -> normalize to "+62-" + without leading 0
    // - International form: +<countryCode>-<number> where countryCode is 2-3 digits and number has 6-14 digits
    // Additionally tolerated (will be normalized):
    // - Unicode dashes (–, —) and spaces will be sanitized
    // - +<countryCode><number> without dash will be normalized to +<cc>-<number>
    public static String normalizeOrThrow(String raw) {
        if (raw == null) throw new IllegalArgumentException("customerPhone is required");
        String s = raw.trim();
        // Normalize to NFKC form to convert fullwidth characters (e.g., fullwidth plus)
        try {
            s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFKC);
        } catch (Throwable ignore) { /* Normalizer may be unavailable in some profiles */ }
        // Sanitize: collapse any dash punctuation to '-' and remove all whitespace
        s = s.replaceAll("[\\p{Pd}]+", "-");
        s = s.replaceAll("\\s+", "");
        if (s.isEmpty()) throw new IllegalArgumentException("customerPhone is required");

        // If starts with 08... (Indonesian style), convert to +62-...
        if (s.matches("^0[0-9]{8,14}$")) {
            return "+62-" + s.substring(1);
        }
        // Already in +62-...
        if (s.matches("^\\+62-[0-9]{8,14}$")) {
            return s;
        }
        // +62 without dash -> normalize to +62-...
        if (s.matches("^\\+62[0-9]{8,14}$")) {
            return "+62-" + s.substring(3);
        }
        // Generic +<cc>-<digits>
        if (s.matches("^\\+[0-9]{2,3}-[0-9]{6,14}$")) {
            return s;
        }
        // Generic +<cc><digits> (no dash)
        if (s.startsWith("+") && !s.contains("-")) {
            String digits = s.substring(1);
            if (digits.matches("[0-9]{8,17}")) { // allow 2-3 cc + 6-14 subscriber
                String cc2 = digits.substring(0, Math.min(2, digits.length()));
                String rest2 = digits.substring(Math.min(2, digits.length()));
                String cc3 = digits.length() >= 3 ? digits.substring(0, 3) : cc2;
                String rest3 = digits.length() >= 3 ? digits.substring(3) : rest2;
                boolean rest2Valid = rest2.length() >= 6 && rest2.length() <= 14;
                boolean rest3Valid = rest3.length() >= 6 && rest3.length() <= 14;
                // Prefer 3-digit country code when it doesn't produce leading zero for subscriber and is valid
                if (rest3Valid && !rest3.startsWith("0")) {
                    return "+" + cc3 + "-" + rest3;
                }
                if (rest2Valid) {
                    return "+" + cc2 + "-" + rest2;
                }
            }
        }
        throw new IllegalArgumentException("Invalid customerPhone format. Allowed: 08XXXXXXXX, +62-XXXXXXXX, or +<cc>-<digits>");
    }
}
