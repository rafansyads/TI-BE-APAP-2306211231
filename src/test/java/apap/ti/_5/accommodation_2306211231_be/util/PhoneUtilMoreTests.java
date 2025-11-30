package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PhoneUtilMoreTests {

    @Test
    void normalize_plus62Dash_kept() {
        assertEquals("+62-81234567890", PhoneUtil.normalizeOrThrow("+62-81234567890"));
    }

    @Test
    void normalize_nullAndBlank_throw() {
        assertThrows(IllegalArgumentException.class, () -> PhoneUtil.normalizeOrThrow(null));
        assertThrows(IllegalArgumentException.class, () -> PhoneUtil.normalizeOrThrow("   "));
    }
}
