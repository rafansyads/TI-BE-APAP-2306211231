package apap.ti._5.accommodation_2306211231_be.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class DateUtil {
    private DateUtil() {}

    public static boolean isOverlapping(java.time.LocalDateTime start1, java.time.LocalDateTime end1,
                                        java.time.LocalDateTime start2, java.time.LocalDateTime end2) {
        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    public static int calculateTotalDays(LocalDateTime checkIn, LocalDateTime checkOut) {
        LocalDateTime normalizedIn = normalizeCheckIn(checkIn);
        LocalDateTime normalizedOut = normalizeCheckOut(checkOut);
        return computeDays(normalizedIn, normalizedOut);
    }

    public static LocalDateTime normalizeCheckIn(LocalDateTime dt) {
        if (dt == null) return null;
        LocalDate d = dt.toLocalDate();
        // Always enforce 14:00 check-in regardless of provided time
        return d.atTime(14, 0);
    }

    public static LocalDateTime normalizeCheckOut(LocalDateTime dt) {
        if (dt == null) return null;
        LocalDate d = dt.toLocalDate();
        // Always enforce 12:00 check-out regardless of provided time
        return d.atTime(12, 0);
    }

    public static int computeDays(LocalDateTime in, LocalDateTime out) {
        if (in == null || out == null) return 0;
        long diff = ChronoUnit.DAYS.between(in.toLocalDate(), out.toLocalDate());
        return (int) Math.max(1, diff);
    }
}
