package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;

class RoomMapperTests {

    @Test
    void fromCreateRequestMapsBasicFields() {
        RoomCreateRequest req = new RoomCreateRequest("PID-101", "101", 1, 1, null, null, null);
        Room room = RoomMapper.fromCreateRequest(req);
        assertEquals("PID-101", room.getRoomId());
        assertEquals("101", room.getName());
        assertEquals(1, room.getAvailabilityStatus());
        assertEquals(1, room.getActiveRoom());
    }

    @Test
    void toDetailDtoMapsFields() {
        Room room = Room.builder().roomId("PID-202").name("202").availabilityStatus(0).activeRoom(1).build();
        RoomDetailDto dto = RoomMapper.toDetailDto(room);
        assertEquals("PID-202", dto.getRoomId());
        assertEquals("202", dto.getName());
        assertEquals(0, dto.getAvailabilityStatus());
        assertEquals(1, dto.getActiveRoom());
    }
}
