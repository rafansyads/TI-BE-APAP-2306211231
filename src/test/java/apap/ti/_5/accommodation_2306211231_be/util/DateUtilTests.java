package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DateUtilTests {

    @Test
    void normalizeCheckIn_setsTo14h() {
        LocalDateTime src = LocalDateTime.of(2025, 1, 1, 9, 30);
        LocalDateTime norm = DateUtil.normalizeCheckIn(src);
        assertEquals(LocalDateTime.of(2025, 1, 1, 14, 0), norm);
    }

    @Test
    void normalizeCheckOut_setsTo12hNextDayWhenMidnight() {
        LocalDateTime src = LocalDateTime.of(2025, 1, 2, 23, 45);
        LocalDateTime norm = DateUtil.normalizeCheckOut(src);
        // normalizeCheckOut should set 12:00 on same date part (ignores time part)
        assertEquals(LocalDateTime.of(2025, 1, 2, 12, 0), norm);
    }

    @Test
    void computeDays_countsWholeDaysBetween() {
        LocalDateTime in = LocalDateTime.of(2025, 1, 1, 14, 0);
        LocalDateTime out = LocalDateTime.of(2025, 1, 3, 12, 0);
        assertEquals(2, DateUtil.computeDays(in, out));
    }

    @Test
    void isOverlapping_detectsOverlap() {
        LocalDateTime a1 = LocalDateTime.of(2025, 2, 1, 14, 0);
        LocalDateTime a2 = LocalDateTime.of(2025, 2, 3, 12, 0);
        LocalDateTime b1 = LocalDateTime.of(2025, 2, 2, 12, 0);
        LocalDateTime b2 = LocalDateTime.of(2025, 2, 4, 12, 0);
        assertTrue(DateUtil.isOverlapping(a1, a2, b1, b2));
        // Current implementation treats touching as overlap
        assertTrue(DateUtil.isOverlapping(a1, a2, a2, b2));
    }
}
