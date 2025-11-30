package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.OwnerSummaryDto;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PropertyRestServiceUnitTest {

    @Test
    void predictNextPropertySequence_emptyRepository_returnsOne() {
        var propRepo = mock(PropertyRepository.class);
        when(propRepo.findAll()).thenReturn(List.of());

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), mock(AccommodationOwnerRepository.class));
        int next = svc.predictNextPropertySequence();
        assertEquals(1, next);
    }

    @Test
    void getOwners_distinctOwnersReturned() {
        var propRepo = mock(PropertyRepository.class);
        Property p1 = new Property(); p1.setOwnerId(UUID.randomUUID()); p1.setOwnerName("Alice");
        Property p2 = new Property(); p2.setOwnerId(UUID.randomUUID()); p2.setOwnerName("Bob");
        Property p3 = new Property(); p3.setOwnerId(p1.getOwnerId()); p3.setOwnerName("Alice");
        when(propRepo.findByDeletedAtIsNull()).thenReturn(List.of(p1,p2,p3));

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), mock(AccommodationOwnerRepository.class));
        List<OwnerSummaryDto> owners = svc.getOwners();
        assertEquals(2, owners.size());
        assertTrue(owners.stream().anyMatch(o -> o.getOwnerName().equals("Alice")));
        assertTrue(owners.stream().anyMatch(o -> o.getOwnerName().equals("Bob")));
    }

    @Test
    void recomputeTotalRooms_countsRoomsAndReturnsDto() {
        var propRepo = mock(PropertyRepository.class);
        Property p = new Property(); p.setPropertyId("P-1");
        RoomType rt = new RoomType();
        Room r1 = new Room(); r1.setRoomId("P-1-101");
        Room r2 = new Room(); r2.setRoomId("P-1-102");
        rt.setListRoom(List.of(r1, r2));
        p.setListRoomType(List.of(rt));
        when(propRepo.findByPropertyIdAndDeletedAtIsNull("P-1")).thenReturn(Optional.of(p));
        when(propRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), mock(AccommodationOwnerRepository.class));
        PropertyDetailDto dto = svc.recomputeTotalRooms("P-1");
        assertNotNull(dto);
        assertEquals(2, dto.getTotalRoom());
        verify(propRepo).save(any());
    }

    @Test
    void getPropertyDetailDto_withDateFilters_marksUnavailableWhenBookingOverlaps() {
        var propRepo = mock(PropertyRepository.class);
        Property p = new Property(); p.setPropertyId("P-2");
        RoomType rt = new RoomType(); rt.setRoomTypeId("RT-1");
        Room r = new Room(); r.setRoomId("P-2-101");
        AccommodationBooking b = new AccommodationBooking();
        b.setCheckInDate(LocalDateTime.parse("2025-12-01T14:00:00"));
        b.setCheckOutDate(LocalDateTime.parse("2025-12-03T12:00:00"));
        b.setStatus(1);
        r.setBookings(List.of(b));
        rt.setListRoom(List.of(r));
        p.setListRoomType(List.of(rt));
        when(propRepo.findByPropertyIdAndDeletedAtIsNull("P-2")).thenReturn(Optional.of(p));

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), mock(AccommodationOwnerRepository.class));
        // Query a range that overlaps the booking
        PropertyDetailDto dto = svc.getPropertyDetailDto("P-2", "2025-12-01", "2025-12-02");
        assertNotNull(dto);
        assertNotNull(dto.getRooms());
        assertFalse(dto.getRooms().isEmpty());
        assertEquals(0, dto.getRooms().get(0).getAvailabilityStatus());
    }

    @Test
    void addMaintenance_overlappingBooking_throws() {
        var propRepo = mock(PropertyRepository.class);
        var roomRepo = mock(RoomRepository.class);
        Property p = new Property(); p.setPropertyId("P-3");
        RoomType rt = new RoomType(); rt.setRoomTypeId("RT-2");
        Room r = new Room(); r.setRoomId("P-3-101");
        AccommodationBooking b = new AccommodationBooking();
        b.setCheckInDate(LocalDateTime.parse("2025-12-10T14:00:00"));
        b.setCheckOutDate(LocalDateTime.parse("2025-12-12T12:00:00"));
        b.setStatus(1);
        r.setBookings(List.of(b));
        rt.setListRoom(List.of(r));
        p.setListRoomType(List.of(rt));
        when(propRepo.findByPropertyIdAndDeletedAtIsNull("P-3")).thenReturn(Optional.of(p));

        var svc = new PropertyRestService(propRepo, roomRepo, mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), mock(AccommodationOwnerRepository.class));

        RoomUpdateRequest req = new RoomUpdateRequest();
        req.setRoomTypeId("RT-2");
        req.setId("P-3-101");
        req.setMaintenanceStart("2025-12-11T00:00:00");
        req.setMaintenanceEnd("2025-12-13T00:00:00");

        assertThrows(IllegalArgumentException.class, () -> svc.addMaintenance(req));
    }
}
