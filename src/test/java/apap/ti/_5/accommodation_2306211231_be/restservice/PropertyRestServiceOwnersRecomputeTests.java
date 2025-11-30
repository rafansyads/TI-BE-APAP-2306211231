package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;

@ExtendWith(MockitoExtension.class)
class PropertyRestServiceOwnersRecomputeTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    @Test
    void getOwners_deduplicatesOwnerIds() {
        UUID owner = UUID.randomUUID();
        Property p1 = Property.builder().propertyId("HOT-ABCD-001").ownerId(owner).ownerName("Alice").build();
        Property p2 = Property.builder().propertyId("HOT-ABCD-002").ownerId(owner).ownerName("Alice").build();
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(p1,p2));
        var owners = service.getOwners();
        assertEquals(1, owners.size());
        assertEquals("Alice", owners.get(0).getOwnerName());
    }

    @Test
    void recomputeTotalRooms_updatesTotal() {
        Property p = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel").totalRoom(0).build();
        RoomType rt1 = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").build();
        RoomType rt2 = RoomType.builder().roomTypeId("001-Std-3").name("Std").build();
        rt1.setListRoom(new ArrayList<>(List.of(Room.builder().roomId("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1).roomType(rt1).build())));
        rt2.setListRoom(new ArrayList<>(List.of(
            Room.builder().roomId("HOT-ABCD-001-301").name("301").availabilityStatus(1).activeRoom(1).roomType(rt2).build(),
            Room.builder().roomId("HOT-ABCD-001-302").name("302").availabilityStatus(1).activeRoom(1).roomType(rt2).build())));
        p.setListRoomType(List.of(rt1, rt2));
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(p));
        when(propertyRepository.save(any(Property.class))).thenAnswer(inv -> inv.getArgument(0));
        var dto = service.recomputeTotalRooms("HOT-ABCD-001");
        assertEquals(3, dto.getTotalRoom());
    }
}
