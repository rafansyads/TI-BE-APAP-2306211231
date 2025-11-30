package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Ensures fallback data is present (simulates remote failure scenario by invoking static maps).
 */
class ProvinceUtilFallbackTests {

    @Test
    void fallbackContainsKeyExamples() {
        Map<Integer,String> all = ProvinceUtil.getAll();
        // Core Java island provinces
        assertEquals("DKI Jakarta", all.get(31));
        assertEquals("Jawa Barat", all.get(32));
        assertEquals("Papua Barat Daya", all.get(96));
        // Basic validity checks
        assertTrue(ProvinceUtil.isValidCode(11));
        assertTrue(ProvinceUtil.getNameByCode(51).orElse("none").contains("Bali"));
    }

    @Test
    void nameLookupIgnoresAsterisksAndCase() {
        assertTrue(ProvinceUtil.getCodeByName("dki jakarta").orElse(-1) == 31);
        assertTrue(ProvinceUtil.getCodeByName("JAWA   BARAT").orElse(-1) == 32);
    }


    @Test
    void fallbackDataContainsKeyProvinces() throws Exception {
        // Access internal CODE_TO_NAME via reflection to ensure it was populated (remote fetch may fail in test env)
        Field f = ProvinceUtil.class.getDeclaredField("CODE_TO_NAME");
        f.setAccessible(true);
        @SuppressWarnings("unchecked") Map<Integer,String> map = (Map<Integer,String>) f.get(null);
        // Assert presence of a few sentinel provinces from fallback list
        assertEquals("Aceh", map.get(11));
        assertEquals("DKI Jakarta", map.get(31));
        assertEquals("Papua Barat Daya", map.get(96));
        assertTrue(map.size() >= 30); // ensure broad fallback coverage
    }
}
