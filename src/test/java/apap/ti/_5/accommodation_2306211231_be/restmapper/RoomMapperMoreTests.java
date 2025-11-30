package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;

class RoomMapperMoreTests {

    @Test
    void updateEntityUpdatesFieldsAndParsesDates() {
        Room r = Room.builder().roomId("RM-X").name("101").availabilityStatus(0).activeRoom(1).build();
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("RM-X").name("102").availabilityStatus(1).activeRoom(0)
            .maintenanceStart(LocalDateTime.now().plusDays(1).toString())
            .maintenanceEnd(LocalDateTime.now().plusDays(2).toString())
            .roomTypeId("RT-1").build();
        RoomMapper.updateEntity(r, req);
        assertEquals("102", r.getName());
        assertEquals(1, r.getAvailabilityStatus());
        assertEquals(0, r.getActiveRoom());
        assertNotNull(r.getMaintenanceStart());
        assertNotNull(r.getMaintenanceEnd());
    }

    @Test
    void toDetailDtoBoundaryAtStartAndEndNow() {
        LocalDateTime now = LocalDateTime.now();
        // At exact start -> unavailable
        Room r1 = Room.builder().roomId("RM-1").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(now).maintenanceEnd(now.plusMinutes(5)).build();
        RoomDetailDto d1 = RoomMapper.toDetailDto(r1);
        assertEquals(0, d1.getAvailabilityStatus());
        // At exact end -> available restored if status was 0
        Room r2 = Room.builder().roomId("RM-2").name("202").availabilityStatus(0).activeRoom(1)
            .maintenanceStart(now.minusMinutes(10)).maintenanceEnd(now).build();
        RoomDetailDto d2 = RoomMapper.toDetailDto(r2);
        assertEquals(1, d2.getAvailabilityStatus());
    }
}
