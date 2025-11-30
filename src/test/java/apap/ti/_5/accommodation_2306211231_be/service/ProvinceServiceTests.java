package apap.ti._5.accommodation_2306211231_be.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProvinceServiceTests {

    private final ProvinceService service = new ProvinceService();

    @Test
    void delegatesToUtil() {
        assertTrue(service.isValidCode(31));
        assertEquals("DKI Jakarta", service.getNameByCode(31).orElse(null));
        assertEquals(31, service.getCodeByName("  dki   jakarta ").orElse(-1));
        assertFalse(service.getAll().isEmpty());
    }
}
