package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceAddMaintenanceTests {

    @Mock private PropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomTypeRepository roomTypeRepository;

    @InjectMocks private PropertyRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;

    @BeforeEach
    void setup() {
        property = Property.builder().propertyId("HOT-ABCD-001").build();
        roomType = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").floor(2).property(property).build();
        room = Room.builder().roomId("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1).roomType(roomType).build();
        roomType.setListRoom(java.util.Arrays.asList(room));
        property.setListRoomType(java.util.Arrays.asList(roomType));
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-ABCD-001")).thenReturn(Optional.of(property));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void addMaintenanceSuccessUpdatesRoom() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-201")
            .name("201")
            .availabilityStatus(1)
            .activeRoom(1)
            .maintenanceStart(start.toString())
            .maintenanceEnd(end.toString())
            .roomTypeId("001-Deluxe-2")
            .build();

        var dto = service.addMaintenance(req);
        assertEquals(start.toString(), dto.getMaintenanceStart());
        assertEquals(end.toString(), dto.getMaintenanceEnd());
    }

    @Test
    void addMaintenanceRejectsEndBeforeStart() {
        LocalDateTime start = LocalDateTime.now().plusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(start.toString()).maintenanceEnd(end.toString()).roomTypeId("001-Deluxe-2").build();
        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }

    @Test
    void addMaintenanceRejectsPastDates() {
        LocalDateTime start = LocalDateTime.now().minusDays(3);
        LocalDateTime end = LocalDateTime.now().plusDays(2);
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("HOT-ABCD-001-201").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(start.toString()).maintenanceEnd(end.toString()).roomTypeId("001-Deluxe-2").build();
        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }

    @Test
    void addMaintenanceRejectsMissingFields() {
        RoomUpdateRequest req = RoomUpdateRequest.builder()
            .id("").name("201").availabilityStatus(1).activeRoom(1)
            .maintenanceStart(null).maintenanceEnd(null).roomTypeId("").build();
        assertThrows(IllegalArgumentException.class, () -> service.addMaintenance(req));
    }
}
