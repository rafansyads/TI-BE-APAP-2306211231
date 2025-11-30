package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import apap.ti._5.accommodation_2306211231_be.models.*;
import apap.ti._5.accommodation_2306211231_be.repository.*;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:flowSuccess;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccommodationBookingRestServiceFlowSuccessTests {

    @Autowired AccommodationBookingRestService service;
    @Autowired PropertyRepository propertyRepository;
    @Autowired RoomTypeRepository roomTypeRepository;
    @Autowired RoomRepository roomRepository;
    @Autowired AccommodationBookingRepository bookingRepository;
    @Autowired apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository customerRepository;

    private Room prepareRoomChain() {
        Property p = propertyRepository.save(Property.builder().propertyId("P-FLOW-1").propertyName("Hotel Flow").type(1)
            .address("A").province(1).activeStatus(1).totalRoom(0).ownerName("Owner").ownerId(UUID.randomUUID()).profit(0).build());
        RoomType rt = roomTypeRepository.save(RoomType.builder().roomTypeId("RT-FLOW-1").name("Deluxe").price(120000).capacity(2).floor(1).property(p).build());
        Room r = roomRepository.save(Room.builder().roomId("RM-FLOW-1-101").name("101").roomType(rt).availabilityStatus(1).activeRoom(1).build());
        return r;
    }

    @Test
    void createPayUpdateRefundFlow_success() {
        Room room = prepareRoomChain();
        // Create booking request
        AccommodationBookingCreateRequest createReq = new AccommodationBookingCreateRequest();
        createReq.setRoomId(room.getRoomId());
        createReq.setCheckInDate(LocalDateTime.now().plusDays(3));
        createReq.setCheckOutDate(LocalDateTime.now().plusDays(5)); // 2 nights
        createReq.setCustomerId(UUID.randomUUID().toString());
        createReq.setCustomerName("Alice");
        createReq.setCustomerEmail("a@a.com");
        createReq.setCustomerPhone("081234567890");
        createReq.setIsBreakfast(true); // adds 50k per day
        createReq.setCapacity(2);
        createReq.setTotalDays(0); // ignored
        createReq.setTotalPrice(0); // ignored
        createReq.setStatus(0);
        createReq.setRefund(0); createReq.setExtraPay(0);

        // Ensure a matching Customer exists (service will lookup by id)
        apap.ti._5.accommodation_2306211231_be.models.profile.Customer cust = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer();
        java.util.UUID cid = java.util.UUID.fromString(createReq.getCustomerId());
        cust.setId(cid); cust.setUsername("u"+cid.toString()); cust.setPassword("p"); cust.setName("Alice"); cust.setEmail("a@a.com"); cust.setGender(true); cust.setSaldo(1_000_000L);
        customerRepository.save(cust);

        var dtoCreated = service.createBooking(createReq);
        assertEquals(2, dtoCreated.getTotalDays());
        // expected total = 2 * (120000 + 50000) = 340000
        assertEquals(340000, dtoCreated.getTotalPrice());
        assertEquals(0, dtoCreated.getStatus());
        String bookingId = dtoCreated.getBookingId();

    // Pay booking
        var dtoPaid = service.markBookingAsPaid(bookingId);
        assertEquals(1, dtoPaid.getStatus());
    // Profit updated on property (reload from repository to ensure fresh state)
    var propAfterPay = propertyRepository.findById(room.getRoomType().getProperty().getPropertyId()).orElseThrow();
    assertEquals(340000, propAfterPay.getProfit());

        // Shorten stay: reduce nights from 2 to 1 triggers refund (status=3)
        AccommodationBookingUpdateRequest upd = new AccommodationBookingUpdateRequest();
        upd.setBookingId(bookingId);
        upd.setCheckInDate(dtoCreated.getCheckInDate());
        upd.setCheckOutDate(dtoCreated.getCheckInDate().plusDays(1)); // now 1 night
        upd.setTotalDays(1);
        upd.setTotalPrice(dtoPaid.getTotalPrice()); // client sends previous total; service keeps previousTotal
        upd.setStatus(1);
        upd.setCustomerId(createReq.getCustomerId());
        upd.setCustomerName("Alice");
        upd.setCustomerEmail("a@a.com");
        upd.setCustomerPhone("081234567890");
        upd.setIsBreakfast(true);
        upd.setRefund(0); upd.setExtraPay(0); // service will compute refund
        upd.setCapacity(2);
        upd.setRoomId(room.getRoomId());
        upd.setPropertyName(room.getRoomType().getProperty().getPropertyName());
        upd.setRoomTypeName(room.getRoomType().getName());
        upd.setRoomName(room.getName());

        var dtoUpdated = service.updateBooking(bookingId, upd);
        assertEquals(3, dtoUpdated.getStatus()); // refund requested
        assertTrue(dtoUpdated.getRefund() > 0);
        int refundAmount = dtoUpdated.getRefund();

        // Process refund
    var dtoRefunded = service.refundBooking(bookingId);
        assertEquals(1, dtoRefunded.getStatus());
        // Profit reduced by refund
    var propAfterRefund = propertyRepository.findById(room.getRoomType().getProperty().getPropertyId()).orElseThrow();
    assertEquals(340000 - refundAmount, propAfterRefund.getProfit());
        assertEquals(dtoPaid.getTotalPrice() - refundAmount, dtoRefunded.getTotalPrice());

        // Cancel booking (status 1) removes remaining profit
    var dtoCanceled = service.cancelBooking(bookingId);
        assertEquals(2, dtoCanceled.getStatus());
    var propAfterCancel = propertyRepository.findById(room.getRoomType().getProperty().getPropertyId()).orElseThrow();
    assertEquals(0, propAfterCancel.getProfit());
    }
}
