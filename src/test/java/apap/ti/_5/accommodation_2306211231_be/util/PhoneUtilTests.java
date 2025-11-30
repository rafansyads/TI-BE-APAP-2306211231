package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PhoneUtilTests {

    @Test
    void indonesianLeadingZeroNormalized() {
        assertEquals("+62-81234567890", PhoneUtil.normalizeOrThrow("081234567890"));
    }

    @Test
    void plus62WithoutDashNormalized() {
        assertEquals("+62-81234567890", PhoneUtil.normalizeOrThrow("+6281234567890"));
    }

    @Test
    void unicodeDashAndSpacesCollapsed() {
        // Using EM DASH and spaces
        assertEquals("+44-2070313000", PhoneUtil.normalizeOrThrow("+44— 2070313000"));
    }

    @Test
    void genericCountryCodesAccepted() {
        assertEquals("+44-2079460958", PhoneUtil.normalizeOrThrow("+44-2079460958"));
        assertEquals("+44-2079460958", PhoneUtil.normalizeOrThrow("+442079460958"));
    }

    @Test
    void invalidThrows() {
        assertThrows(IllegalArgumentException.class, () -> PhoneUtil.normalizeOrThrow("abc"));
    }
}
