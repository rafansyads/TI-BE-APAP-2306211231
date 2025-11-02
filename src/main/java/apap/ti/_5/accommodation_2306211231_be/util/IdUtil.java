package apap.ti._5.accommodation_2306211231_be.util;

import java.util.Locale;
import java.util.UUID;

public final class IdUtil {
    private IdUtil() {}

    public static String generatePropertyId(int type, UUID ownerId, long sequence) {
        String prefix = switch (type) {
            case 1 -> "HOT";
            case 2 -> "VIL";
            case 3 -> "APT";
            default -> "UNK";
        };
        String ownerHex = ownerId.toString().replace("-", "").toUpperCase(Locale.ROOT);
        String last4 = ownerHex.substring(ownerHex.length() - 4);
        String seq3 = String.format("%03d", sequence);
        return prefix + "-" + last4 + "-" + seq3;
    }

    public static int extractPropertyCounter(String propertyId) {
        if (propertyId == null || propertyId.length() < 3) return 0;
        String[] parts = propertyId.split("-");
        if (parts.length < 3) return 0;
        try { return Integer.parseInt(parts[2]); } catch (NumberFormatException e) { return 0; }
    }

    public static String generateRoomTypeId(String propertyId, String roomTypeName, int floor) {
        int counter = extractPropertyCounter(propertyId);
        String counter3 = String.format("%03d", counter);
        return counter3 + "-" + roomTypeName + "-" + floor;
    }

    public static String generateRoomId(String propertyId, int floor, int unitIndex) {
        // Floor numbering: 1..n, unitIndex: 1..n
        int roomNumber = floor * 100 + unitIndex; // yields 101, 1001, 10002, etc.
        return propertyId + "-" + roomNumber;
    }

    public static String fetchPropertyIdFromRoomId(String roomId) {
        if (roomId == null || roomId.length() < 5) return null;
        int lastDash = roomId.lastIndexOf("-");
        if (lastDash == -1) return null;
        return roomId.substring(0, lastDash);
    }
}
