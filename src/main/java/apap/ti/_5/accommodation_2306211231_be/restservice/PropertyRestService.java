package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomTypeRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PropertyRestService {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    // Placeholders to be implemented later
    public List<Property> getAllProperties() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Optional<Property> getPropertyById(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Property createProperty(Property property) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Property updateProperty(String propertyId, Property property) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteProperty(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // Room & RoomType placeholders to demonstrate repository availability
    public List<Room> getRoomsByProperty(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<RoomType> getRoomTypesByProperty(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // DTO-based method signatures for controllers (stubs)
    public List<PropertySummaryDto> getAllPropertiesDto() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PropertyDetailDto getPropertyDetailDto(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PropertyDetailDto createProperty(PropertyCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PropertyDetailDto updateProperty(String propertyId, PropertyUpdateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void softDeleteProperty(String propertyId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
