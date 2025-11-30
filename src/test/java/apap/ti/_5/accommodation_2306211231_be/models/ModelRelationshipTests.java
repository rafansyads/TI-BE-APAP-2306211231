package apap.ti._5.accommodation_2306211231_be.models;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

class ModelRelationshipTests {

    @Test
    void propertyAddRemoveRoomTypeMaintainsLinks() {
        Property p = Property.builder().propertyId("PID").build();
        RoomType rt = RoomType.builder().roomTypeId("RTID").name("T").floor(1).build();
        p.setListRoomType(new ArrayList<>());

        p.addRoomType(rt);
        assertEquals(1, p.getListRoomType().size());
        assertEquals(p, rt.getProperty());

        p.removeRoomType(rt);
        assertEquals(0, p.getListRoomType().size());
        assertNull(rt.getProperty());
    }

    @Test
    void roomTypeAddRemoveRoomMaintainsLinks() {
        RoomType rt = RoomType.builder().roomTypeId("RTID").name("T").floor(1).build();
        Room r = Room.builder().roomId("RID").name("101").availabilityStatus(1).activeRoom(1).build();
        rt.setListRoom(new ArrayList<>());

        rt.addRoom(r);
        assertEquals(1, rt.getListRoom().size());
        assertEquals(rt, r.getRoomType());

        rt.removeRoom(r);
        assertEquals(0, rt.getListRoom().size());
        assertNull(r.getRoomType());
    }
}
