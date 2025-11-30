package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PropertyRestServiceOwnerConsistencyTests {

    @Mock private PropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private RoomTypeRepository roomTypeRepository;
    @Mock private apap.ti._5.accommodation_2306211231_be.repository.profile.AccommodationOwnerRepository accommodationOwnerRepository;

    @InjectMocks private PropertyRestService service;

    @Test
    void createPropertyRejectsMismatchedOwner() {
        UUID owner = UUID.randomUUID();
        Property existing = Property.builder().ownerId(owner).ownerName("Correct").build();
        when(propertyRepository.findFirstByOwnerId(owner)).thenReturn(Optional.of(existing));

        // Stub owner repository to return a profile with the expected name
        apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner ownerProfile =
            new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
        ownerProfile.setId(owner);
        ownerProfile.setName("Correct");
        when(accommodationOwnerRepository.findById(owner)).thenReturn(Optional.of(ownerProfile));

        PropertyCreateRequest req = new PropertyCreateRequest();
        req.setPropertyName("X");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(31);
        req.setTotalRoom(0);
        req.setActiveStatus(1);
        req.setOwnerName("Wrong");
        req.setOwnerId(owner.toString());
        // Minimal nested to pass pre-checks
        var rt = new apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest();
        rt.setName("Type"); rt.setPrice(1); rt.setCapacity(1); rt.setFloor(1);
        var r = new apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest(null, null, 1, 1, null, null, null);
        rt.setRooms(java.util.List.of(r));
        req.setRoomTypes(java.util.List.of(rt));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.createProperty(req));
        assertTrue(ex.getMessage().toLowerCase().contains("owner uuid/name mismatch"));
    }

    @Test
    void updatePropertyRejectsMismatchedOwner() {
        String pid = "HOT-ABCD-001";
        // existing property must already belong to the same owner UUID
        UUID owner = UUID.randomUUID();
        Property existingProp = Property.builder().propertyId(pid).ownerId(owner).ownerName("Correct").build();
        when(propertyRepository.findByPropertyIdAndDeletedAtIsNull(pid)).thenReturn(Optional.of(existingProp));
        Property other = Property.builder().ownerId(owner).ownerName("Correct").build();
        when(propertyRepository.findFirstByOwnerId(owner)).thenReturn(Optional.of(other));

        // Stub owner repository to return a profile with the expected name
        apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner ownerProfile =
            new apap.ti._5.accommodation_2306211231_be.models.profile.AccommodationOwner();
        ownerProfile.setId(owner);
        ownerProfile.setName("Correct");
        when(accommodationOwnerRepository.findById(owner)).thenReturn(Optional.of(ownerProfile));

        PropertyUpdateRequest req = new PropertyUpdateRequest();
        req.setPropertyId(pid);
        req.setPropertyName("X");
        req.setType(1);
        req.setAddress("Addr");
        req.setProvince(31);
        req.setTotalRoom(0);
        req.setActiveStatus(1);
        req.setOwnerName("Wrong");
        req.setOwnerId(owner.toString());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.updateProperty(pid, req));
        assertTrue(ex.getMessage().toLowerCase().contains("owner uuid/name mismatch"));
    }
}
