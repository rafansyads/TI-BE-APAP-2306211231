package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest.RoomTypeCreateRequestBuilder;
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

public class PropertyRestServiceCreateUpdateUnitTest {

    @Test
    void createProperty_invalidProvince_throws() {
        var propRepo = mock(PropertyRepository.class);
        var ownerRepo = mock(AccommodationOwnerRepository.class);
        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), ownerRepo);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X"); req.setType(1); req.setAddress("addr"); req.setProvince(9999); req.setTotalRoom(1); req.setActiveStatus(1);
        req.setOwnerId(UUID.randomUUID().toString()); req.setOwnerName("Owner");

        assertThrows(IllegalArgumentException.class, () -> svc.createProperty(req));
    }

    @Test
    void createProperty_ownerNotFound_throws() {
        var propRepo = mock(PropertyRepository.class);
        var ownerRepo = mock(AccommodationOwnerRepository.class);
        when(ownerRepo.findById(any())).thenReturn(Optional.empty());
        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), ownerRepo);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X"); req.setType(1); req.setAddress("addr"); req.setProvince(31); req.setTotalRoom(1); req.setActiveStatus(1);
        req.setOwnerId(UUID.randomUUID().toString()); req.setOwnerName("Owner");
        // include minimal room type
        RoomTypeCreateRequest rt = new RoomTypeCreateRequest(); rt.setName("T"); rt.setPrice(100); rt.setCapacity(1); rt.setFloor(1);
        rt.setRooms(List.of(new RoomCreateRequest(null, null, 1,1,null,null,null)));
        req.setRoomTypes(List.of(rt));

        assertThrows(IllegalArgumentException.class, () -> svc.createProperty(req));
    }

    @Test
    void createProperty_duplicateRoomTypeName_throws() {
        var propRepo = mock(PropertyRepository.class);
        var ownerRepo = mock(AccommodationOwnerRepository.class);
        AccommodationOwner owner = new AccommodationOwner(); owner.setId(UUID.randomUUID()); owner.setName("Owner");
        when(ownerRepo.findById(any())).thenReturn(Optional.of(owner));
        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), ownerRepo);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X"); req.setType(1); req.setAddress("addr"); req.setProvince(31); req.setTotalRoom(1); req.setActiveStatus(1);
        req.setOwnerId(owner.getId().toString()); req.setOwnerName(owner.getName());
        RoomTypeCreateRequest rt1 = new RoomTypeCreateRequest(); rt1.setName("T"); rt1.setPrice(100); rt1.setCapacity(1); rt1.setFloor(1);
        rt1.setRooms(List.of(new RoomCreateRequest(null, null, 1,1,null,null,null)));
        RoomTypeCreateRequest rt2 = new RoomTypeCreateRequest(); rt2.setName("T"); rt2.setPrice(150); rt2.setCapacity(1); rt2.setFloor(1);
        rt2.setRooms(List.of(new RoomCreateRequest(null, null, 1,1,null,null,null)));
        req.setRoomTypes(List.of(rt1, rt2));

        assertThrows(IllegalArgumentException.class, () -> svc.createProperty(req));
    }

    @Test
    void updateProperty_priceChange_updatesBookingsAndSaves() {
        var propRepo = mock(PropertyRepository.class);
        var bookingRepo = mock(AccommodationBookingRepository.class);

        Property existing = new Property(); existing.setPropertyId("P-4");
        RoomType rt = new RoomType(); rt.setRoomTypeId("RT-4"); rt.setPrice(100);
        Room room = new Room(); room.setRoomId("P-4-101");
        AccommodationBooking b = new AccommodationBooking();
        b.setStatus(1); // paid
        b.setCheckInDate(LocalDateTime.parse("2025-12-01T00:00:00"));
        b.setCheckOutDate(LocalDateTime.parse("2025-12-02T00:00:00"));
        room.setBookings(List.of(b));
        rt.setListRoom(List.of(room));
        existing.setListRoomType(List.of(rt));

        when(propRepo.findByPropertyIdAndDeletedAtIsNull("P-4")).thenReturn(Optional.of(existing));
        when(propRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), bookingRepo, mock(AccommodationOwnerRepository.class));

        // Build update request: same roomTypeId, price changed
        var req = new apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest();
        var rtReq = new apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest();
        rtReq.setRoomTypeId("RT-4"); rtReq.setPrice(150);
        req.setRoomTypes(List.of(rtReq));

        var dto = svc.updateProperty("P-4", req);
        // booking should have been mutated: status -> 0 and extraPay set
        assertEquals(0, b.getStatus());
        assertTrue(b.getExtraPay() != null && b.getExtraPay() > 0);
        verify(bookingRepo).save(any());
    }

    @Test
    void createProperty_roomTypeMissingRooms_throws() {
        var propRepo = mock(PropertyRepository.class);
        var ownerRepo = mock(AccommodationOwnerRepository.class);
        AccommodationOwner owner = new AccommodationOwner(); owner.setId(UUID.randomUUID()); owner.setName("Owner");
        when(ownerRepo.findById(any())).thenReturn(Optional.of(owner));
        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), ownerRepo);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X"); req.setType(1); req.setAddress("addr"); req.setProvince(31); req.setTotalRoom(1); req.setActiveStatus(1);
        req.setOwnerId(owner.getId().toString()); req.setOwnerName(owner.getName());
        RoomTypeCreateRequest rt = new RoomTypeCreateRequest(); rt.setName("T"); rt.setPrice(100); rt.setCapacity(1); rt.setFloor(1);
        rt.setRooms(List.of()); // empty rooms -> should throw
        req.setRoomTypes(List.of(rt));

        assertThrows(IllegalArgumentException.class, () -> svc.createProperty(req));
    }

    @Test
    void createProperty_roomTypeFloorOutOfRange_throws() {
        var propRepo = mock(PropertyRepository.class);
        var ownerRepo = mock(AccommodationOwnerRepository.class);
        AccommodationOwner owner = new AccommodationOwner(); owner.setId(UUID.randomUUID()); owner.setName("Owner");
        when(ownerRepo.findById(any())).thenReturn(Optional.of(owner));
        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), mock(AccommodationBookingRepository.class), ownerRepo);

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X"); req.setType(1); req.setAddress("addr"); req.setProvince(31); req.setTotalRoom(1); req.setActiveStatus(1);
        req.setOwnerId(owner.getId().toString()); req.setOwnerName(owner.getName());
        RoomTypeCreateRequest rt = new RoomTypeCreateRequest(); rt.setName("T"); rt.setPrice(100); rt.setCapacity(1); rt.setFloor(10);
        rt.setRooms(List.of(new RoomCreateRequest(null, null, 1,1,null,null,null)));
        req.setRoomTypes(List.of(rt));

        assertThrows(IllegalArgumentException.class, () -> svc.createProperty(req));
    }

    @Test
    void updateProperty_priceDecrease_refundAndStatusChange() {
        var propRepo = mock(PropertyRepository.class);
        var bookingRepo = mock(AccommodationBookingRepository.class);

        Property existing = new Property(); existing.setPropertyId("P-5");
        RoomType rt = new RoomType(); rt.setRoomTypeId("RT-5"); rt.setPrice(150);
        Room room = new Room(); room.setRoomId("P-5-101");
        AccommodationBooking b = new AccommodationBooking();
        b.setStatus(1); // paid
        b.setCheckInDate(LocalDateTime.parse("2025-12-01T00:00:00"));
        b.setCheckOutDate(LocalDateTime.parse("2025-12-02T00:00:00"));
        room.setBookings(List.of(b));
        rt.setListRoom(List.of(room));
        existing.setListRoomType(List.of(rt));

        when(propRepo.findByPropertyIdAndDeletedAtIsNull("P-5")).thenReturn(Optional.of(existing));
        when(propRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var svc = new PropertyRestService(propRepo, mock(RoomRepository.class), mock(RoomTypeRepository.class), bookingRepo, mock(AccommodationOwnerRepository.class));

        var req = new apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest();
        var rtReq = new apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest();
        rtReq.setRoomTypeId("RT-5"); rtReq.setPrice(100);
        req.setRoomTypes(List.of(rtReq));

        var dto = svc.updateProperty("P-5", req);
        assertEquals(3, b.getStatus());
        assertTrue(b.getRefund() != null && b.getRefund() > 0);
        verify(bookingRepo).save(any());
    }
}
