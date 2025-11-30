package apap.ti._5.accommodation_2306211231_be.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class IdUtilTests {

    @Test
    void generatePropertyIdAndExtractCounter() {
        UUID owner = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        String id = IdUtil.generatePropertyId(1, owner, 7);
        assertTrue(id.startsWith("HOT-"));
        assertTrue(id.endsWith("-007"));
        assertEquals(7, IdUtil.extractPropertyCounter(id));
    }

    @Test
    void generateRoomTypeIdUsesPropertyCounterAndProps() {
        String pid = "HOT-ABCD-011";
        String rt = IdUtil.generateRoomTypeId(pid, "Deluxe", 2);
        assertEquals("011-Deluxe-2", rt);
    }

    @Test
    void generateRoomIdAndFetchPropertyId() {
        String pid = "APT-FFFF-123";
        String roomId = IdUtil.generateRoomId(pid, 3, 5);
        assertEquals("APT-FFFF-123-305", roomId);
        assertEquals(pid, IdUtil.fetchPropertyIdFromRoomId(roomId));
    }
}
