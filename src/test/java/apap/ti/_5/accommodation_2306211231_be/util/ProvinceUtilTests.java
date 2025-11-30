package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ProvinceUtilTests {

    @Test
    void isValidCode_knownAndUnknown() {
        assertTrue(ProvinceUtil.isValidCode(31));
        assertFalse(ProvinceUtil.isValidCode(0));
        assertFalse(ProvinceUtil.isValidCode(null));
    }

    @Test
    void nameToCode_roundTrip_caseInsensitive_andNormalization() {
        assertEquals("DKI Jakarta", ProvinceUtil.getNameByCode(31).orElse(null));
        assertEquals(31, ProvinceUtil.getCodeByName("dki  jakarta").orElse(-1));
        // odd spacing and marks
        assertEquals(34, ProvinceUtil.getCodeByName("Daerah   Istimewa   Yogyakarta").orElse(-1));
        assertEquals(31, ProvinceUtil.getCodeByName("DKI Jakarta*").orElse(-1));
        assertEquals(31, ProvinceUtil.getCodeByName("DKI+ Jakarta").orElse(-1));
    }

    @Test
    void getAll_containsEntriesAndPapuaBaratDaya() {
        var all = ProvinceUtil.getAll();
        assertFalse(all.isEmpty());
        assertTrue(all.containsKey(31));
        assertTrue(ProvinceUtil.isValidCode(96));
        assertEquals(96, ProvinceUtil.getCodeByName("Papua Barat Daya").orElse(-1));
    }
}
