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
import apap.ti._5.accommodation_2306211231_be.restmapper.PropertyMapper;
import apap.ti._5.accommodation_2306211231_be.util.ProvinceUtil;
import apap.ti._5.accommodation_2306211231_be.util.IdUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyRestService {

    private final PropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;

    public long count() {
        return propertyRepository.countByDeletedAtIsNull();
    }

    // Placeholders to be implemented later
    public List<Property> getAllProperties() {
        return propertyRepository.findByDeletedAtIsNull();
    }

    public Optional<Property> getPropertyById(String propertyId) {
        return propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId);
    }

    public Property createProperty(Property property) {
        return propertyRepository.save(property);
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

    // DTO-based methods for controllers
    public List<PropertySummaryDto> getAllPropertiesDto() {
        return propertyRepository.findByDeletedAtIsNull().stream()
                .map(PropertyMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    public PropertyDetailDto getPropertyDetailDto(String propertyId) {
        Property p = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        return PropertyMapper.toDetailDto(p);
    }

    public PropertyDetailDto createProperty(PropertyCreateRequest request) {
        
        if (request == null) throw new IllegalArgumentException("Request cannot be null");
        if (!ProvinceUtil.isValidCode(request.getProvince())) {
            throw new IllegalArgumentException("Invalid province code: " + request.getProvince());
        }
        Property entity = PropertyMapper.fromCreateRequest(request);
        
        // Generate ID on backend per spec
        long seq = propertyRepository.count() + 1; // regardless of owner
        var ownerUuid = UUID.fromString(request.getOwnerId());
        String generatedId = IdUtil.generatePropertyId(request.getType(), ownerUuid, seq);
        entity.setPropertyId(generatedId);
        
        Property saved = propertyRepository.save(entity);
        return PropertyMapper.toDetailDto(saved);
    }

    public PropertyDetailDto updateProperty(String propertyId, PropertyUpdateRequest request) {
        Property existing = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        if (request.getProvince() != null && !ProvinceUtil.isValidCode(request.getProvince())) {
            throw new IllegalArgumentException("Invalid province code: " + request.getProvince());
        }
        PropertyMapper.updateEntity(existing, request);
        Property saved = propertyRepository.save(existing);
        return PropertyMapper.toDetailDto(saved);
    }

    public void softDeleteProperty(String propertyId) {
        Property existing = propertyRepository.findByPropertyIdAndDeletedAtIsNull(propertyId)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + propertyId));
        existing.setDeletedAt(LocalDateTime.now());
        propertyRepository.save(existing);
    }
}
