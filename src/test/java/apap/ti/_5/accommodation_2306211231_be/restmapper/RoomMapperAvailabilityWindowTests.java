package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;

class RoomMapperAvailabilityWindowTests {

    @Test
    void availabilityForcedUnavailableDuringMaintenanceWindow() {
        LocalDateTime start = LocalDateTime.now().minusMinutes(5);
        LocalDateTime end = LocalDateTime.now().plusMinutes(30);
        Room room = Room.builder()
            .roomId("RM-1")
            .name("101")
            .availabilityStatus(1) // normally available
            .activeRoom(1)
            .maintenanceStart(start)
            .maintenanceEnd(end)
            .build();
        RoomDetailDto dto = RoomMapper.toDetailDto(room);
        assertEquals(0, dto.getAvailabilityStatus(), "Room should be forced unavailable during maintenance interval");
    }

    @Test
    void availabilityRestoredAfterMaintenanceWindowEnds() {
        LocalDateTime start = LocalDateTime.now().minusHours(2);
        LocalDateTime end = LocalDateTime.now().minusMinutes(1); // ended just before now
        Room room = Room.builder()
            .roomId("RM-2")
            .name("102")
            .availabilityStatus(0) // stored unavailable but should flip to 1 after end per mapper logic
            .activeRoom(1)
            .maintenanceStart(start)
            .maintenanceEnd(end)
            .build();
        RoomDetailDto dto = RoomMapper.toDetailDto(room);
        assertEquals(1, dto.getAvailabilityStatus(), "Room should be available again after maintenance end passed");
    }

    @Test
    void availabilityUnchangedWhenNoMaintenance() {
        Room room = Room.builder()
            .roomId("RM-3")
            .name("103")
            .availabilityStatus(1)
            .activeRoom(1)
            .build();
        RoomDetailDto dto = RoomMapper.toDetailDto(room);
        assertEquals(1, dto.getAvailabilityStatus());
    }
}
