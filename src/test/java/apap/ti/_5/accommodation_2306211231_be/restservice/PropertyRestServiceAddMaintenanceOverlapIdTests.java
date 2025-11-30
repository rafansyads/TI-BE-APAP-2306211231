package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;

@ExtendWith(MockitoExtension.class)
class PropertyRestServiceAddMaintenanceOverlapIdTests {
    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;
    @InjectMocks PropertyRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setup() {
        property = Property.builder().propertyId("HOT-ABCD-001").listRoomType(new ArrayList<>()).build();
        roomType = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").listRoom(new ArrayList<>()).property(property).build();
        room = Room.builder().roomId("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1).roomType(roomType).build();
        roomType.getListRoom().add(room);
        property.getListRoomType().add(roomType);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
    }

    @Test
    void addMaintenance_overlapsExistingBooking_throws() {
        // existing booking overlaps maintenance window
        AccommodationBooking b = new AccommodationBooking();
        b.setStatus(1); // active booking
        b.setCheckInDate(LocalDateTime.now().plusDays(5).withHour(14));
        b.setCheckOutDate(LocalDateTime.now().plusDays(7).withHour(12));
        room.setBookings(new ArrayList<>(List.of(b)));

        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomTypeId(roomType.getRoomTypeId())
            .maintenanceStart(LocalDateTime.now().plusDays(6).toString())
            .maintenanceEnd(LocalDateTime.now().plusDays(6).plusHours(2).toString())
            .build();

        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }

    @Test
    void addMaintenance_wrongRoomTypeId_throws() {
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomTypeId("WRONG-RT")
            .maintenanceStart(LocalDateTime.now().plusDays(5).toString())
            .maintenanceEnd(LocalDateTime.now().plusDays(6).toString())
            .build();
        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }

    @Test
    void addMaintenance_wrongRoomId_throws() {
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-999")
            .name("999")
            .availabilityStatus(1)
            .activeRoom(1)
            .roomTypeId(roomType.getRoomTypeId())
            .maintenanceStart(LocalDateTime.now().plusDays(5).toString())
            .maintenanceEnd(LocalDateTime.now().plusDays(6).toString())
            .build();
        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }
}
