package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ProvinceUtilMoreTests {

    @Test
    void nullAndBlankInputs_returnEmptyOptionals() {
        assertTrue(ProvinceUtil.getNameByCode(null).isEmpty());
        assertTrue(ProvinceUtil.getCodeByName(null).isEmpty());
        assertTrue(ProvinceUtil.getCodeByName("   ").isEmpty());
        assertFalse(ProvinceUtil.isValidCode(null));
        assertFalse(ProvinceUtil.isValidCode(999));
    }

    @Test
    void getAll_isUnmodifiable() {
        Map<Integer,String> all = ProvinceUtil.getAll();
        assertFalse(all.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> all.put(10, "X"));
    }

    @Test
    void normalizationCoversSpacingAndCase() {
        assertEquals(53, ProvinceUtil.getCodeByName("  NUSA   TENGGARA   TIMUR  ").orElse(-1));
        assertEquals("Nusa Tenggara Timur", ProvinceUtil.getNameByCode(53).orElse(null));
        // Not found returns empty optional
        assertTrue(ProvinceUtil.getCodeByName("Unknown Province").isEmpty());
    }
}
