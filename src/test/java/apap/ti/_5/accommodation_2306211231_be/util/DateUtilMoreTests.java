package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DateUtilMoreTests {

    @Test
    void normalizeCheckIn_after14_kept() {
        LocalDateTime src = LocalDateTime.of(2025, 1, 1, 16, 15);
        assertEquals(src, DateUtil.normalizeCheckIn(src));
    }

    @Test
    void computeDays_sameDay_minOne() {
        LocalDateTime in = LocalDateTime.of(2025, 2, 2, 14, 0);
        LocalDateTime out = LocalDateTime.of(2025, 2, 2, 12, 0);
        assertEquals(1, DateUtil.computeDays(in, out));
    }
}
