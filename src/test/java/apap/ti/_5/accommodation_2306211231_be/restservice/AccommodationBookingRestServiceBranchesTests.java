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
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.repository.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceBranchesTests {

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock CustomerRestService customerService;

    @InjectMocks AccommodationBookingRestService service;

    private Property property;
    private RoomType roomType;
    private Room room;
    private AccommodationBooking booking;
    private UUID customerId;

    @BeforeEach
    void setup() {
        customerId = UUID.randomUUID();

        property = new Property();
        property.setPropertyId("HOT-TEST-001");
        property.setPropertyName("Test Hotel");
        property.setProfit(0);
        property.setType(1);

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

        booking = new AccommodationBooking();
        booking.setBookingId("B001");
        booking.setRoom(room);
        booking.setCustomerId(customerId);
        booking.setStatus(1);
        booking.setTotalPrice(200000);
        booking.setCheckInDate(LocalDateTime.now().plusDays(1));
        booking.setCheckOutDate(LocalDateTime.now().plusDays(3));

        // Default customer stub
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setSaldo(1000000L);
        when(customerService.findById(any(UUID.class))).thenReturn(Optional.of(customer));
    }

    // ===== getBookingChart branches =====

    @Test
    void getBookingChart_nullMonth_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getBookingChart(null, 2024, null));
    }

    @Test
    void getBookingChart_nullYear_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getBookingChart(5, null, null));
    }

    @Test
    void getBookingChart_withOwnerId_filtersProperties() {
        UUID ownerId = UUID.randomUUID();
        property.setOwnerId(ownerId);

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of());

        var result = service.getBookingChart(5, 2024, ownerId);
        assertNotNull(result);
        assertEquals(5, result.get("month"));
        assertEquals(2024, result.get("year"));
    }

    @Test
    void getBookingChart_ownerIdNotMatching_excludesProperty() {
        UUID ownerId = UUID.randomUUID();
        property.setOwnerId(UUID.randomUUID()); // different owner

        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of());

        var result = service.getBookingChart(5, 2024, ownerId);
        assertNotNull(result);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertTrue(labels.isEmpty());
    }

    @Test
    void getBookingChart_bookingWithNullRoom_skipped() {
        booking.setRoom(null);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        assertNotNull(result);
    }

    @Test
    void getBookingChart_bookingWithNullRoomType_skipped() {
        room.setRoomType(null);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        assertNotNull(result);
    }

    @Test
    void getBookingChart_bookingWithNullProperty_skipped() {
        roomType.setProperty(null);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        assertNotNull(result);
    }

    @Test
    void getBookingChart_bookingWithNullCheckIn_skipped() {
        booking.setCheckInDate(null);
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        assertNotNull(result);
    }

    @Test
    void getBookingChart_wrongYear_notCounted() {
        booking.setCheckInDate(LocalDateTime.of(2023, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_wrongMonth_notCounted() {
        booking.setCheckInDate(LocalDateTime.of(2024, 6, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_status0_notCounted() {
        booking.setStatus(0);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_status2_notCounted() {
        booking.setStatus(2);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_status1_counted() {
        booking.setStatus(1);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        booking.setTotalPrice(200000);
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(200000, data.get(0));
    }

    @Test
    void getBookingChart_status3_counted() {
        booking.setStatus(3);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(200000, data.get(0));
    }

    @Test
    void getBookingChart_status4_counted() {
        booking.setStatus(4);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(200000, data.get(0));
    }

    @Test
    void getBookingChart_statusNull_treatedAs0_notCounted() {
        booking.setStatus(null);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_nullTotalPrice_treatedAsZero() {
        booking.setStatus(1);
        booking.setTotalPrice(null);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, null);
        @SuppressWarnings("unchecked")
        List<Integer> data = (List<Integer>) result.get("data");
        assertEquals(0, data.get(0));
    }

    @Test
    void getBookingChart_ownerFilter_excludesBookingsFromOtherOwners() {
        UUID ownerId = UUID.randomUUID();
        property.setOwnerId(UUID.randomUUID()); // different owner
        booking.setStatus(1);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, ownerId);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertTrue(labels.isEmpty());
    }

    @Test
    void getBookingChart_ownerFilter_nullPropertyOwner_skipped() {
        UUID ownerId = UUID.randomUUID();
        property.setOwnerId(null);
        booking.setStatus(1);
        booking.setCheckInDate(LocalDateTime.of(2024, 5, 15, 14, 0));
        
        when(propertyRepository.findByDeletedAtIsNull()).thenReturn(List.of(property));
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getBookingChart(5, 2024, ownerId);
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) result.get("labels");
        assertTrue(labels.isEmpty());
    }

    // ===== count =====

    @Test
    void count_returnsCorrectValue() {
        when(bookingRepository.count()).thenReturn(10L);

        long result = service.count();
        assertEquals(10L, result);
    }

    // ===== getAllBookings =====

    @Test
    void getAllBookings_returnsAllBookings() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getAllBookings();
        assertEquals(1, result.size());
    }

    // ===== getBookingById =====

    @Test
    void getBookingById_found() {
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        var result = service.getBookingById("B001");
        assertTrue(result.isPresent());
    }

    @Test
    void getBookingById_notFound() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        var result = service.getBookingById("INVALID");
        assertTrue(result.isEmpty());
    }

    // ===== getAllBookingsDto =====

    @Test
    void getAllBookingsDto_mapsToDto() {
        when(bookingRepository.findAll()).thenReturn(List.of(booking));

        var result = service.getAllBookingsDto();
        assertEquals(1, result.size());
    }

    // ===== getBookingDtoById =====

    @Test
    void getBookingDtoById_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getBookingDtoById(null));
    }

    @Test
    void getBookingDtoById_blankId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getBookingDtoById("  "));
    }

    @Test
    void getBookingDtoById_notFound_throwsException() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(Exception.class,
            () -> service.getBookingDtoById("INVALID"));
    }

    @Test
    void getBookingDtoById_found_returnsDto() {
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        var result = service.getBookingDtoById("B001");
        assertNotNull(result);
    }

    // ===== createBooking entity =====

    @Test
    void createBooking_entity_saves() {
        when(bookingRepository.save(booking)).thenReturn(booking);

        var result = service.createBooking(booking);
        assertNotNull(result);
    }

    // ===== refundBooking branches =====

    @Test
    void refundBooking_nullBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.refundBooking(null));
    }

    @Test
    void refundBooking_blankBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.refundBooking("  "));
    }

    @Test
    void refundBooking_notFound_throwsException() {
        when(bookingRepository.findById("INVALID")).thenReturn(Optional.empty());

        assertThrows(Exception.class,
            () -> service.refundBooking("INVALID"));
    }

    @Test
    void refundBooking_status0_throwsException() {
        booking.setStatus(0);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_status2_throwsException() {
        booking.setStatus(2);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_status4_throwsException() {
        booking.setStatus(4);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_refundZero_throwsException() {
        booking.setStatus(3);
        booking.setRefund(0);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_refundNegative_throwsException() {
        booking.setStatus(3);
        booking.setRefund(-100);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalArgumentException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_nullRoom_throwsException() {
        booking.setStatus(3);
        booking.setRefund(50000);
        booking.setRoom(null);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_nullRoomType_throwsException() {
        booking.setStatus(3);
        booking.setRefund(50000);
        room.setRoomType(null);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_nullProperty_throwsException() {
        booking.setStatus(3);
        booking.setRefund(50000);
        roomType.setProperty(null);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.refundBooking("B001"));
    }

    @Test
    void refundBooking_valid_status3_success() {
        booking.setStatus(3);
        booking.setRefund(50000);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.refundBooking("B001");
        assertNotNull(result);
        assertEquals(1, result.getStatus()); // back to paid after refund
    }

    @Test
    void refundBooking_valid_status1_success() {
        booking.setStatus(1);
        booking.setRefund(50000);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.refundBooking("B001");
        assertNotNull(result);
    }

    @Test
    void refundBooking_nullProfit_treatedAsZero() {
        booking.setStatus(3);
        booking.setRefund(50000);
        property.setProfit(null);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.refundBooking("B001");
        assertNotNull(result);
    }

    @Test
    void refundBooking_nullTotalPrice_treatedAsZero() {
        booking.setStatus(3);
        booking.setRefund(50000);
        booking.setTotalPrice(null);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.refundBooking("B001");
        assertNotNull(result);
    }

    @Test
    void refundBooking_nullCustomerId_noCustomerUpdate() {
        booking.setStatus(3);
        booking.setRefund(50000);
        booking.setCustomerId(null);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.refundBooking("B001");
        assertNotNull(result);
        verify(customerService, never()).findById(any());
    }

    @Test
    void refundBooking_customerNotFound_continuesWithoutError() {
        booking.setStatus(3);
        booking.setRefund(50000);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.findById(any())).thenReturn(Optional.empty());

        var result = service.refundBooking("B001");
        assertNotNull(result);
    }

    @Test
    void refundBooking_customerUpdateThrows_continuesWithoutError() {
        booking.setStatus(3);
        booking.setRefund(50000);
        property.setProfit(200000);
        
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setSaldo(100000L);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.findById(any())).thenReturn(Optional.of(customer));
        doThrow(new RuntimeException("DB error")).when(customerService).update(any());

        var result = service.refundBooking("B001");
        assertNotNull(result);
    }

    // ===== cancelBooking branches =====

    @Test
    void cancelBooking_nullBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.cancelBooking(null));
    }

    @Test
    void cancelBooking_blankBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.cancelBooking("  "));
    }

    @Test
    void cancelBooking_status4_throwsException() {
        booking.setStatus(4);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.cancelBooking("B001"));
    }

    @Test
    void cancelBooking_status2_throwsException() {
        booking.setStatus(2);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.cancelBooking("B001"));
    }

    @Test
    void cancelBooking_status0_success() {
        booking.setStatus(0);
        property.setProfit(100000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancelBooking("B001");
        assertNotNull(result);
        assertEquals(2, result.getStatus());
    }

    @Test
    void cancelBooking_status1_refundsAndCancels() {
        booking.setStatus(1);
        booking.setTotalPrice(150000);
        booking.setExtraPay(0);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancelBooking("B001");
        assertNotNull(result);
        assertEquals(2, result.getStatus());
    }

    @Test
    void cancelBooking_status1_withExtraPay_partialRefund() {
        booking.setStatus(1);
        booking.setTotalPrice(150000);
        booking.setExtraPay(50000); // extra not refunded
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancelBooking("B001");
        assertNotNull(result);
    }

    @Test
    void cancelBooking_status3_refundsWholePaid() {
        booking.setStatus(3);
        booking.setTotalPrice(150000);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancelBooking("B001");
        assertNotNull(result);
        assertEquals(2, result.getStatus());
    }

    @Test
    void cancelBooking_nullRoom_throwsException() {
        booking.setStatus(1);
        booking.setRoom(null);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.cancelBooking("B001"));
    }

    @Test
    void cancelBooking_nullCustomerId_noCustomerUpdate() {
        booking.setStatus(1);
        booking.setTotalPrice(150000);
        booking.setCustomerId(null);
        property.setProfit(200000);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.cancelBooking("B001");
        assertNotNull(result);
    }

    // ===== markBookingAsPaid branches =====

    @Test
    void markBookingAsPaid_nullBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.markBookingAsPaid(null));
    }

    @Test
    void markBookingAsPaid_blankBookingId_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> service.markBookingAsPaid("  "));
    }

    @Test
    void markBookingAsPaid_statusNot0_throwsException() {
        booking.setStatus(1);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.markBookingAsPaid("B001"));
    }

    @Test
    void markBookingAsPaid_nullRoom_throwsException() {
        booking.setStatus(0);
        booking.setRoom(null);
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
            () -> service.markBookingAsPaid("B001"));
    }

    @Test
    void markBookingAsPaid_valid_success() {
        booking.setStatus(0);
        booking.setTotalPrice(200000);
        booking.setExtraPay(0);
        booking.setRefund(0);
        property.setProfit(0);
        
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setSaldo(500000L);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.findById(any())).thenReturn(Optional.of(customer));

        var result = service.markBookingAsPaid("B001");
        assertNotNull(result);
        assertEquals(1, result.getStatus());
    }

    @Test
    void markBookingAsPaid_withExtraPay_addsToTotal() {
        booking.setStatus(0);
        booking.setTotalPrice(200000);
        booking.setExtraPay(50000);
        booking.setRefund(0);
        property.setProfit(0);
        
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setSaldo(500000L);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.findById(any())).thenReturn(Optional.of(customer));

        var result = service.markBookingAsPaid("B001");
        assertNotNull(result);
    }

    @Test
    void markBookingAsPaid_withRefund_subtractsFromTotal() {
        booking.setStatus(0);
        booking.setTotalPrice(200000);
        booking.setExtraPay(0);
        booking.setRefund(50000);
        property.setProfit(0);
        
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setSaldo(500000L);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerService.findById(any())).thenReturn(Optional.of(customer));

        var result = service.markBookingAsPaid("B001");
        assertNotNull(result);
    }

    @Test
    void markBookingAsPaid_nullCustomerId_noCustomerDebit() {
        booking.setStatus(0);
        booking.setTotalPrice(200000);
        booking.setCustomerId(null);
        property.setProfit(0);
        
        when(bookingRepository.findById("B001")).thenReturn(Optional.of(booking));
        when(propertyRepository.save(any())).thenReturn(property);
        when(bookingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var result = service.markBookingAsPaid("B001");
        assertNotNull(result);
    }
}
