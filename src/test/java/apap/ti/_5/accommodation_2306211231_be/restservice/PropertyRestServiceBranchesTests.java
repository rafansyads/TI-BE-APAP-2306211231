package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.repository.*;
import apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceBranchesTests {

    @Mock PropertyRepository propertyRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomTypeRepository roomTypeRepository;
    @Mock AccommodationBookingRepository bookingRepository;
    @Mock AccommodationOwnerRepository ownerRepository;

    @InjectMocks PropertyRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;
    private UUID ownerId;

    @BeforeEach
    void setup() {
        ownerId = UUID.randomUUID();

        property = new Property();
        property.setPropertyId("HOT-TEST-001");
        property.setPropertyName("Test Hotel");
        property.setOwnerId(ownerId);
        property.setOwnerName("Test Owner");
        property.setProvince(31); // Jakarta
        property.setType(1);
        property.setProfit(0);

        roomType = new RoomType();
        roomType.setRoomTypeId("HOT-TEST-001-Std-1");
        roomType.setName("Standard");
        roomType.setFloor(1);
        roomType.setPrice(100000);
        roomType.setCapacity(2);
        roomType.setProperty(property);
        roomType.setListRoom(new ArrayList<>());

        room = new Room();
        room.setRoomId("HOT-TEST-001-101");
        room.setName("101");
        room.setActiveRoom(1);
        room.setAvailabilityStatus(1);
        room.setRoomType(roomType);
        room.setBookings(new ArrayList<>());

        roomType.setListRoom(List.of(room));
        property.setListRoomType(List.of(roomType));
    }

    // ===== getAllPropertiesDto branches =====

    @Test
    void getAllPropertiesDto_nullListRoomType() {
        property.setListRoomType(null);
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_nullListRoom() {
        roomType.setListRoom(null);
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_nullBookings() {
        room.setBookings(null);
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_bookingWithNullStatus() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(null);
        booking.setCheckInDate(LocalDateTime.now().minusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(1));
        room.setBookings(List.of(booking));

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_bookingWithStatus2_ignored() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(2); // canceled
        booking.setCheckInDate(LocalDateTime.now().minusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(1));
        room.setBookings(List.of(booking));

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_bookingWithNullDates() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(1);
        booking.setCheckInDate(null);
        booking.setCheckOutDate(null);
        room.setBookings(List.of(booking));

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    @Test
    void getAllPropertiesDto_ongoingBooking_marksUnavailable() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(1); // paid
        booking.setCheckInDate(LocalDateTime.now().minusHours(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(1));
        room.setBookings(List.of(booking));

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllPropertiesDto();
        assertEquals(1, result.size());
    }

    // ===== getPropertyDetailDto with dates =====

    @Test
    void getPropertyDetailDto_withBothDates_filtersRooms() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_withOnlyCheckIn_createsWindow() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", null);
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_withOnlyCheckOut_createsWindow() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", null, "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_withBlankDates_returnsCanonical() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "", "");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_roomWithActiveZero_markedUnavailable() {
        room.setActiveRoom(0);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_bookingStatus4PastCheckout_ignored() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(4); // done
        booking.setCheckInDate(LocalDateTime.now().minusDays(5));
        booking.setCheckOutDate(LocalDateTime.now().minusDays(1)); // past checkout
        room.setBookings(List.of(booking));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_bookingStatus4NotPast_considered() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(4); // done
        booking.setCheckInDate(LocalDateTime.now().minusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(5)); // still active
        room.setBookings(List.of(booking));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", 
            LocalDateTime.now().toLocalDate().toString(), 
            LocalDateTime.now().plusDays(2).toLocalDate().toString());
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_bookingWithNullStatus_skipped() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(null);
        room.setBookings(List.of(booking));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_status2NotConsidered() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(2); // canceled
        booking.setCheckInDate(LocalDateTime.now());
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));
        room.setBookings(List.of(booking));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_overlappingBooking_marksUnavailable() {
        AccommodationBooking booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setStatus(1); // paid - should be considered
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 16, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2024, 12, 18, 12, 0));
        room.setBookings(List.of(booking));

        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        // overlapping date range
        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    @Test
    void getPropertyDetailDto_nullListRoomType_returnsDto() {
        property.setListRoomType(null);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001", "2024-12-15", "2024-12-20");
        assertNotNull(result);
    }

    // ===== recomputeTotalRooms =====

    @Test
    void recomputeTotalRooms_nullListRoomType_returnsZero() {
        property.setListRoomType(null);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.recomputeTotalRooms("HOT-TEST-001");
        assertEquals(0, result.getTotalRoom());
    }

    @Test
    void recomputeTotalRooms_nullListRoom_countsZero() {
        roomType.setListRoom(null);
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.recomputeTotalRooms("HOT-TEST-001");
        assertEquals(0, result.getTotalRoom());
    }

    @Test
    void recomputeTotalRooms_withRooms_countsCorrectly() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));
        when(propertyRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.recomputeTotalRooms("HOT-TEST-001");
        assertEquals(1, result.getTotalRoom());
    }

    // ===== getOwners =====

    @Test
    void getOwners_nullOwnerId_skipped() {
        property.setOwnerId(null);
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getOwners();
        assertTrue(result.isEmpty());
    }

    @Test
    void getOwners_nullOwnerName_skipped() {
        property.setOwnerName(null);
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getOwners();
        assertTrue(result.isEmpty());
    }

    @Test
    void getOwners_duplicateOwners_deduped() {
        Property prop2 = new Property();
        prop2.setPropertyId("HOT-TEST-002");
        prop2.setOwnerId(ownerId); // same owner
        prop2.setOwnerName("Test Owner");

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property, prop2));

        var result = service.getOwners();
        assertEquals(1, result.size());
    }

    @Test
    void getOwners_multipleOwners_returned() {
        Property prop2 = new Property();
        prop2.setPropertyId("HOT-TEST-002");
        prop2.setOwnerId(UUID.randomUUID()); // different owner
        prop2.setOwnerName("Another Owner");

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property, prop2));

        var result = service.getOwners();
        assertEquals(2, result.size());
    }

    // ===== count =====

    @Test
    void count_returnsCorrectValue() {
        when(propertyRepository.countByDeletedAtIsNull()).thenReturn(5L);

        long result = service.count();
        assertEquals(5L, result);
    }

    // ===== getAllProperties =====

    @Test
    void getAllProperties_returnsProperties() {
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));

        var result = service.getAllProperties();
        assertEquals(1, result.size());
    }

    // ===== getPropertyById =====

    @Test
    void getPropertyById_found() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyById("HOT-TEST-001");
        assertTrue(result.isPresent());
    }

    @Test
    void getPropertyById_notFound() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("INVALID"))
            .thenReturn(Optional.empty());

        var result = service.getPropertyById("INVALID");
        assertTrue(result.isEmpty());
    }

    // ===== createProperty entity =====

    @Test
    void createProperty_entity_saves() {
        when(propertyRepository.save(property)).thenReturn(property);

        var result = service.createProperty(property);
        assertNotNull(result);
    }

    // ===== getPropertyDetailDto without dates =====

    @Test
    void getPropertyDetailDto_notFound_throwsException() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("INVALID"))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.getPropertyDetailDto("INVALID"));
    }

    @Test
    void getPropertyDetailDto_found_returnsDto() {
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull("HOT-TEST-001"))
            .thenReturn(Optional.of(property));

        var result = service.getPropertyDetailDto("HOT-TEST-001");
        assertNotNull(result);
    }
}
