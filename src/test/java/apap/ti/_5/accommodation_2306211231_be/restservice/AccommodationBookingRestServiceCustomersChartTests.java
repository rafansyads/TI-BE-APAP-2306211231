package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccommodationBookingRestServiceCustomersChartTests {
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

    @Test
    void getCustomers_deduplicatesByCustomerId() {
        var b1 = new AccommodationBooking();
        b1.setCustomerId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        b1.setCustomerName("Alice"); b1.setCustomerEmail("a@a.com"); b1.setCustomerPhone("081");
        var b2 = new AccommodationBooking();
        b2.setCustomerId(java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"));
        b2.setCustomerName("Alice"); b2.setCustomerEmail("a@a.com"); b2.setCustomerPhone("081");
        when(bookingRepository.findAll()).thenReturn(List.of(b1, b2));
        var list = service.getCustomers();
        assertEquals(1, list.size());
        assertEquals("Alice", list.get(0).getCustomerName());
    }

    @Test
    void getBookingChart_nullParams_throw() {
        assertThrows(IllegalArgumentException.class, () -> service.getBookingChart(null, 2025, null));
        assertThrows(IllegalArgumentException.class, () -> service.getBookingChart(1, null, null));
    }
}
