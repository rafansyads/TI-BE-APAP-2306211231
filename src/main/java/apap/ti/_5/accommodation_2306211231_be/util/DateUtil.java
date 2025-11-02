package apap.ti._5.accommodation_2306211231_be.util;

public final class DateUtil {
    private DateUtil() {}

    public static boolean isOverlapping(java.time.LocalDateTime start1, java.time.LocalDateTime end1,
                                        java.time.LocalDateTime start2, java.time.LocalDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

}
