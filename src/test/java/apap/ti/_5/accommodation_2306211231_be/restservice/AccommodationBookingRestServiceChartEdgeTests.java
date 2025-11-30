package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

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
    "spring.datasource.url=jdbc:h2:mem:chartedge;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccommodationBookingRestServiceChartEdgeTests {

    @Autowired AccommodationBookingRestService service;
    @Autowired PropertyRepository propertyRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired AccommodationBookingRepository bookingRepository;

    @Test
    void bookingChart_noBookings_returnsEmptyLists() {
        // Ensure repositories are empty for isolation
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
        propertyRepository.deleteAll();
    // With no properties, labels should be empty
    Map<String,Object> chart = service.getBookingChart(LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear(), null);
    assertTrue(((java.util.List<?>)chart.get("labels")).isEmpty());
    assertTrue(((java.util.List<?>)chart.get("values")).isEmpty());
    }

    @Test
    void bookingChart_singlePropertyAggregatesProfit() {
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        roomTypeRepository.deleteAll();
        propertyRepository.deleteAll();
        // Setup property/room chain
        Property p = propertyRepository.save(Property.builder().propertyId("P-CHART-1").propertyName("Hotel One").type(1)
            .address("A").province(1).activeStatus(1).totalRoom(0).ownerName("O").ownerId(UUID.randomUUID()).profit(0).build());
        RoomType rt = roomTypeRepository.save(RoomType.builder().roomTypeId("RT-C1").name("Std").price(100000).capacity(2).floor(1).property(p).build());
        Room r = roomRepository.save(Room.builder().roomId("RM-C1").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build());

        // Paid booking should add its totalPrice to profit via markBookingAsPaid
        // Use current date to avoid month/year boundary flakiness in tests
        LocalDateTime now = LocalDateTime.now();
        AccommodationBooking b = AccommodationBooking.builder()
            .bookingId("BK-CHART-1")
            .room(r)
            .checkInDate(now)
            .checkOutDate(now.plusDays(1))
            .totalDays(1)
            .totalPrice(150000)
            .status(0)
            .customerId(UUID.randomUUID())
            .customerName("Alice")
            .customerEmail("a@a.com")
            .customerPhone("+62-81234567890")
            .isBreakfast(true)
            .refund(0)
            .extraPay(0)
            .capacity(2)
            .build();
        bookingRepository.save(b);
        // Mark booking as paid and update property profit so chart reflects recognized income.
        b.setStatus(1);
        b.setTotalPrice(150000);
        bookingRepository.save(b);
        p.setProfit(150000);
        propertyRepository.save(p);

        Map<String,Object> chart = service.getBookingChart(LocalDateTime.now().getMonthValue(), LocalDateTime.now().getYear(), null);
        java.util.List<?> labels = (java.util.List<?>)chart.get("labels");
        java.util.List<?> values = (java.util.List<?>)chart.get("values");
        // Chart income uses booking total AFTER payment (totalPrice mutated). We paid 150000 so property profit updated, but chart reflects booking totalPrice.
        assertEquals(1, labels.size());
        assertEquals("Hotel One", labels.get(0));
        assertEquals(150000, values.get(0));
    }
}
