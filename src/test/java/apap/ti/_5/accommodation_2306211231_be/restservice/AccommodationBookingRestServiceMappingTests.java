package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceMappingTests {

    @org.junit.jupiter.api.BeforeEach
    void baseStubs() {
        when(customerService.findById(any(java.util.UUID.class))).thenAnswer(inv -> {
            java.util.UUID id = inv.getArgument(0);
            apap.ti._5.accommodation_2306211231_be.models.profile.Customer c = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer();
            c.setId(id); c.setSaldo(1L);
            return java.util.Optional.of(c);
        });
    }

    @Mock AccommodationBookingRepository bookingRepository;
    @Mock RoomRepository roomRepository;
    @Mock PropertyRepository propertyRepository;
    @Mock apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService customerService;
    @InjectMocks AccommodationBookingRestService service;

    private AccommodationBooking buildBooking(String id) {
        Property p = Property.builder().propertyId("HOT-ABCD-001").propertyName("Hotel A").build();
        RoomType rt = RoomType.builder().roomTypeId("001-Deluxe-2").name("Deluxe").price(100).property(p).build();
        Room r = Room.builder().roomId("HOT-ABCD-001-201").name("201").roomType(rt).build();
        AccommodationBooking b = new AccommodationBooking();
        b.setBookingId(id);
        b.setCheckInDate(LocalDateTime.now().plusDays(5));
        b.setCheckOutDate(LocalDateTime.now().plusDays(7));
        b.setTotalDays(2);
        b.setTotalPrice(200);
        b.setStatus(0);
        b.setCustomerId(UUID.randomUUID());
        b.setCustomerName("Bob");
        b.setCustomerEmail("bob@example.com");
        b.setCustomerPhone("08123456789");
        b.setIsBreakfast(false);
        b.setRefund(0);
        b.setExtraPay(0);
        b.setCapacity(2);
        b.setRoom(r);
        return b;
    }

    @Test
    void getAllBookingsDto_mapsEntities() {
        var b = buildBooking("B-1");
        when(bookingRepository.findAll()).thenReturn(List.of(b));
        var list = service.getAllBookingsDto();
        assertEquals(1, list.size());
        var dto = list.get(0);
        assertEquals("B-1", dto.getBookingId());
        assertEquals(200, dto.getTotalPrice());
        assertEquals("Hotel A", dto.getPropertyName());
        assertEquals("Deluxe", dto.getRoomTypeName());
        assertEquals("201", dto.getRoomName());
    }

    @Test
    void getBookingDtoById_success_and_notFound() {
        var b = buildBooking("B-2");
        when(bookingRepository.findById("B-2")).thenReturn(Optional.of(b));
        var dto = service.getBookingDtoById("B-2");
        assertEquals("B-2", dto.getBookingId());
        assertEquals(2, dto.getTotalDays());
        // not found branch
        when(bookingRepository.findById("MISSING")).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.getBookingDtoById("MISSING"));
    }
}
