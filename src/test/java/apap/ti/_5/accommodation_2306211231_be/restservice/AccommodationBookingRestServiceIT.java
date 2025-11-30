package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.repository.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testsvc;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccommodationBookingRestServiceIT {

    @Autowired AccommodationBookingRestService bookingService;
    @Autowired PropertyRepository propertyRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired AccommodationBookingRepository bookingRepository;

    @Test
    void customers_aggregatesDistinctByCustomerId() {
        // Setup minimal property/room chain
        Property prop = Property.builder().propertyId("HOT-XYZ-001").propertyName("Hotel X").type(1)
                .address("A").province(31).activeStatus(1).totalRoom(0).ownerName("Owner").ownerId(java.util.UUID.randomUUID()).profit(0).build();
        prop = propertyRepository.save(prop);
        RoomType rt = RoomType.builder().roomTypeId("RT-X").name("Standard").price(80000).capacity(2).floor(1).property(prop).build();
        rt = roomTypeRepository.save(rt);
        Room room = Room.builder().roomId("RM-X-101").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        room = roomRepository.save(room);
        rt.setListRoom(List.of(room));
        prop.setListRoomType(List.of(rt));

        // Two bookings with same customer id
        var b1 = new AccommodationBooking();
        b1.setBookingId("BK1"); b1.setRoom(room);
        b1.setCustomerId(java.util.UUID.randomUUID());
        b1.setCustomerName("Alice"); b1.setCustomerEmail("a@a.com"); b1.setCustomerPhone("+62");
        b1.setCheckInDate(LocalDateTime.now().plusDays(3)); b1.setCheckOutDate(LocalDateTime.now().plusDays(4));
    b1.setTotalDays(1); b1.setTotalPrice(0); b1.setStatus(0);
    b1.setIsBreakfast(false); b1.setRefund(0); b1.setExtraPay(0); b1.setCapacity(1);
        bookingRepository.save(b1);

        var b2 = new AccommodationBooking();
        b2.setBookingId("BK2"); b2.setRoom(room);
        b2.setCustomerId(b1.getCustomerId()); // same
        b2.setCustomerName("Alice"); b2.setCustomerEmail("a@a.com"); b2.setCustomerPhone("+62");
        b2.setCheckInDate(LocalDateTime.now().plusDays(5)); b2.setCheckOutDate(LocalDateTime.now().plusDays(6));
    b2.setTotalDays(1); b2.setTotalPrice(0); b2.setStatus(0);
    b2.setIsBreakfast(false); b2.setRefund(0); b2.setExtraPay(0); b2.setCapacity(1);
        bookingRepository.save(b2);

        var customers = bookingService.getCustomers();
        assertEquals(1, customers.size());
        assertEquals("Alice", customers.get(0).getCustomerName());
    }
}
