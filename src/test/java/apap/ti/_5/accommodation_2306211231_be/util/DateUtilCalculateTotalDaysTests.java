package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class DateUtilCalculateTotalDaysTests {

    @Test
    void earlyMorningCheckIn_normalizedTo14_andDayCount() {
        LocalDateTime in = LocalDateTime.of(2025, 11, 10, 8, 0); // before 14:00
        LocalDateTime out = LocalDateTime.of(2025, 11, 12, 18, 30); // will normalize to 12:00 same day
        int days = DateUtil.calculateTotalDays(in, out);
        // in normalized to 14:00 Nov 10, out normalized to 12:00 Nov 12 -> diff 2 days -> max(1,diff)=2
        assertEquals(2, days);
    }

    @Test
    void lateAfternoonCheckIn_notNormalized() {
        LocalDateTime in = LocalDateTime.of(2025, 11, 10, 16, 0); // >=14 stays
        LocalDateTime out = LocalDateTime.of(2025, 11, 11, 23, 0); // normalized to 12:00 Nov 11
        int days = DateUtil.calculateTotalDays(in, out);
        // diff in dates: Nov10 -> Nov11 =1
        assertEquals(1, days);
    }

    @Test
    void sameDayCheckInOut_minimumOneDay() {
        LocalDateTime in = LocalDateTime.of(2025, 11, 10, 15, 0);
        LocalDateTime out = LocalDateTime.of(2025, 11, 10, 18, 0); // normalized to 12:00 same day
        int days = DateUtil.calculateTotalDays(in, out);
        assertEquals(1, days);
    }

    @Test
    void nullInputsYieldZero() {
        assertEquals(0, DateUtil.computeDays(null, LocalDateTime.now()));
        assertEquals(0, DateUtil.computeDays(LocalDateTime.now(), null));
    }
}
