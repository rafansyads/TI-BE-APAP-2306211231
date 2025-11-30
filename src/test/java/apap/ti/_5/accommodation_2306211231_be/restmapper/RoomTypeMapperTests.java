package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeSummaryDto;

class RoomTypeMapperTests {

    @Test
    void toSummaryDtoMapsBasicFields() {
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(100000).capacity(2).floor(1).build();
        RoomTypeSummaryDto dto = RoomTypeMapper.toSummaryDto(rt);
        assertEquals("RT-1", dto.getRoomTypeId());
        assertEquals("Deluxe", dto.getName());
        assertEquals(100000, dto.getPrice());
        assertEquals(2, dto.getCapacity());
    }

    @Test
    void toDetailDtoIncludesRoomsViaRoomMapper() {
        Room room = Room.builder().roomId("RM-1").name("101").availabilityStatus(1).activeRoom(1).build();
        RoomType rt = RoomType.builder().roomTypeId("RT-1").name("Deluxe").price(100000).capacity(2).floor(1)
            .listRoom(List.of(room)).build();
        RoomTypeDetailDto dto = RoomTypeMapper.toDetailDto(rt);
        assertEquals("RT-1", dto.getRoomTypeId());
        assertNotNull(dto.getRooms());
        assertEquals(1, dto.getRooms().size());
        assertEquals("RM-1", dto.getRooms().get(0).getRoomId());
    }

    @Test
    void fromCreateRequestAndUpdateEntityRoundtrip() {
        RoomTypeCreateRequest req = RoomTypeCreateRequest.builder()
            .roomTypeId("RT-2").name("Std").price(50000).description("desc").capacity(2).facility("wifi").floor(2)
            .build();
        RoomType entity = RoomTypeMapper.fromCreateRequest(req);
        assertEquals("RT-2", entity.getRoomTypeId());
        assertEquals("Std", entity.getName());

        RoomTypeUpdateRequest upd = new RoomTypeUpdateRequest();
        upd.setName("Std+BF"); upd.setPrice(60000); upd.setCapacity(2); upd.setDescription("d2"); upd.setFacility("w+bf"); upd.setFloor(2);
        RoomTypeMapper.updateEntity(entity, upd);
        assertEquals(60000, entity.getPrice());
        assertEquals("Std+BF", entity.getName());
    }

    @Test
    void toDetailDtoIncludesRoomsAndPropertyId() {
        var prop = apap.ti._5.accommodation_2306211231_be.models.Property.builder().propertyId("HOT-AAAA-001").propertyName("Hotel A").build();
        RoomType rt = RoomType.builder().roomTypeId("001-Standard-1").name("Standard").price(1).capacity(1).floor(1).property(prop).build();
        Room r = Room.builder().roomId("HOT-AAAA-001-101").name("101").availabilityStatus(1).activeRoom(1).roomType(rt).build();
        rt.setListRoom(List.of(r));
        RoomTypeDetailDto dto = RoomTypeMapper.toDetailDto(rt);
        assertEquals("Standard", dto.getName());
        assertEquals(1, dto.getRooms().size());
        assertEquals("HOT-AAAA-001", dto.getPropertyId());
    }
}
