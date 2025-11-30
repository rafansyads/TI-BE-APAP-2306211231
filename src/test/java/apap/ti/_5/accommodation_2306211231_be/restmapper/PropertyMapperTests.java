package apap.ti._5.accommodation_2306211231_be.restmapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;

class PropertyMapperTests {

    @Test
    void toSummaryDtoMapsBasicFields() {
        Property p = Property.builder()
                .propertyId("HOT-ABCD-001")
                .propertyName("Sample")
                .type(1)
                .province(31)
                .activeStatus(1)
                .totalRoom(3)
                .build();
        PropertySummaryDto dto = PropertyMapper.toSummaryDto(p);
        assertEquals("Sample", dto.getPropertyName());
        assertEquals(1, dto.getType());
        assertEquals(31, dto.getProvince());
        assertEquals("DKI Jakarta", dto.getProvinceName());
        assertEquals(1, dto.getActiveStatus());
        assertEquals(3, dto.getTotalRoom());
    }

    @Test
    void toDetailDtoFlattensRoomTypesAndRooms() {
        Property p = Property.builder()
                .propertyId("HOT-AAAA-001")
                .propertyName("Hotel A")
                .type(1)
                .address("Addr")
                .province(31)
                .description("Desc")
                .totalRoom(2)
                .activeStatus(1)
                .ownerName("Alice")
                .ownerId(UUID.randomUUID())
                .build();

        RoomType rt = RoomType.builder()
                .roomTypeId("001-Deluxe-2")
                .name("Deluxe")
                .price(100)
                .capacity(2)
                .floor(2)
                .property(p)
                .build();

        Room r1 = Room.builder()
                .roomId("HOT-AAAA-001-201")
                .name("201")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(rt)
                .build();
        Room r2 = Room.builder()
                .roomId("HOT-AAAA-001-202")
                .name("202")
                .availabilityStatus(1)
                .activeRoom(1)
                .roomType(rt)
                .build();
        rt.setListRoom(List.of(r1, r2));
        p.setListRoomType(List.of(rt));

        PropertyDetailDto dto = PropertyMapper.toDetailDto(p);
        assertEquals("Hotel A", dto.getPropertyName());
        assertEquals(1, dto.getRoomTypes().size());
        assertEquals(2, dto.getRooms().size());
        assertEquals("DKI Jakarta", dto.getProvinceName());
        assertEquals("Deluxe", dto.getRoomTypes().get(0).getName());
        assertEquals("201", dto.getRooms().get(0).getName());
    }
}
