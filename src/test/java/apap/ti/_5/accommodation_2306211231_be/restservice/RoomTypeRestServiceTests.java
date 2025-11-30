package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class RoomTypeRestServiceTests {

    @Mock RoomTypeRepository roomTypeRepository;
    @InjectMocks RoomTypeRestService service;

    @Test
    void getDetailDto_returnsRoomsAndBasicFields() {
        Property prop = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").build();
        RoomType rt = RoomType.builder()
                .roomTypeId("001-Deluxe-2")
                .name("Deluxe")
                .price(100000)
                .description("Spacious room")
                .capacity(2)
                .facility("AC, TV")
                .floor(2)
                .property(prop)
                .build();
        Room r1 = Room.builder().roomId("HOT-ABCD-001-201").name("201").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        Room r2 = Room.builder().roomId("HOT-ABCD-001-202").name("202").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        rt.setListRoom(List.of(r1, r2));

        when(roomTypeRepository.findById(rt.getRoomTypeId())).thenReturn(Optional.of(rt));

        RoomTypeDetailDto dto = service.getDetailDto(rt.getRoomTypeId());
        assertNotNull(dto);
        assertEquals(rt.getRoomTypeId(), dto.getRoomTypeId());
        assertEquals("Deluxe", dto.getName());
        assertEquals(100000, dto.getPrice());
        assertEquals("Spacious room", dto.getDescription());
        assertEquals(2, dto.getCapacity());
        assertEquals("AC, TV", dto.getFacility());
        assertEquals(2, dto.getFloor());
        assertEquals(prop.getPropertyId(), dto.getPropertyId());
        assertNotNull(dto.getRooms());
        assertEquals(2, dto.getRooms().size());
        assertTrue(dto.getRooms().stream().anyMatch(r -> r.getRoomId().equals("HOT-ABCD-001-201")));
    }

    @Test
    void getDetailDto_missingRoomType_throws() {
        when(roomTypeRepository.findById("NOPE")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getDetailDto("NOPE"));
    }
}
