package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccommodationFlowIntegrationTest {

    @Autowired AccommodationBookingRestService bookingService;
    @Autowired PropertyRepository propertyRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired RoomRepository roomRepository;

    @Test
    @Disabled("Pending end-to-end env wiring; revisit after stabilizing test datasource/profile overrides.")
    void endToEnd_create_pay_cancel_updatesProfitAndStatus() {
        // Arrange: create property -> room type -> room persisted
    // Minimal required fields for Property entity (type 1=hotel, address/province/owner fields)
    Property prop = Property.builder()
        .propertyId("HOT-ABCD-001")
        .propertyName("Hotel A")
        .type(1)
        .address("Jl. Test 123")
        .province(1)
        .description("Test property")
        .totalRoom(0)
        .activeStatus(1)
        .ownerName("Owner A")
        .ownerId(java.util.UUID.randomUUID())
        .profit(0)
        .build();
        prop = propertyRepository.save(prop);
    RoomType rt = RoomType.builder()
        .roomTypeId("001-Deluxe-2")
        .name("Deluxe")
        .price(100000)
        .capacity(2)
        .floor(2)
        .property(prop)
        .build();
        rt = roomTypeRepository.save(rt);
        Room room = Room.builder().roomId("HOT-ABCD-001-201").name("201").roomType(rt).availabilityStatus(1).activeRoom(1).build();
        room = roomRepository.save(room);
        rt.setListRoom(List.of(room));
        prop.setListRoomType(List.of(rt));

        // Create booking request for 1 night with breakfast
        AccommodationBookingCreateRequest req = new AccommodationBookingCreateRequest();
        req.setRoomId(room.getRoomId());
        req.setRoomName("201");
        req.setRoomTypeName("Deluxe");
        req.setPropertyName("Hotel A");
        req.setCheckInDate(LocalDateTime.now().plusDays(2).withHour(9));
        req.setCheckOutDate(LocalDateTime.now().plusDays(3).withHour(10));
        req.setIsBreakfast(Boolean.TRUE);
        req.setCapacity(2);

        AccommodationBookingDto created = bookingService.createBooking(req);
        assertNotNull(created.getBookingId());
        assertEquals(1, created.getTotalDays());
        assertEquals(150000, created.getTotalPrice()); // 100k + 50k breakfast
        assertEquals(0, created.getStatus()); // waiting

        // Pay the booking -> profit recognized
        var paid = bookingService.markBookingAsPaid(created.getBookingId());
        assertEquals(1, paid.getStatus());
        var updatedProp = propertyRepository.findById(prop.getPropertyId()).orElseThrow();
        assertEquals(150000, updatedProp.getProfit());

        // Cancel booking -> profit rolled back and status set to 2
        var canceled = bookingService.cancelBooking(created.getBookingId());
        assertEquals(2, canceled.getStatus());
        updatedProp = propertyRepository.findById(prop.getPropertyId()).orElseThrow();
        assertEquals(0, updatedProp.getProfit());
    }
}
