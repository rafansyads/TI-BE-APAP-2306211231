package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.property.PropertyUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertyDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.property.PropertySummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeSummaryDto;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PropertyMapper {
    private PropertyMapper() {}

    public static PropertySummaryDto toSummaryDto(Property p) {
        if (p == null) return null;
        return new PropertySummaryDto(
                p.getPropertyId(),
                p.getPropertyName(),
                p.getType(),
                p.getProvince(),
                p.getActiveStatus()
        );
    }

    public static PropertyDetailDto toDetailDto(Property p) {
    if (p == null) return null;
    List<RoomTypeSummaryDto> roomTypes = null;
    List<RoomSummaryDto> rooms = null;
    if (p.getListRoomType() != null) {
        roomTypes = p.getListRoomType().stream()
            .map(PropertyMapper::toRoomTypeSummary)
            .collect(Collectors.toList());
        rooms = p.getListRoomType().stream()
            .filter(rt -> rt.getListRoom() != null)
            .flatMap(rt -> rt.getListRoom().stream())
            .map(r -> new RoomSummaryDto(
                r.getRoomId(),
                r.getName(),
                r.getAvailabilityStatus(),
                r.getActiveRoom(),
                r.getRoomType() != null ? r.getRoomType().getRoomTypeId() : null
            ))
            .collect(Collectors.toList());
    }

    var dto = new PropertyDetailDto();
    dto.setPropertyId(p.getPropertyId());
    dto.setPropertyName(p.getPropertyName());
    dto.setType(p.getType());
    dto.setAddress(p.getAddress());
    dto.setProvince(p.getProvince());
    dto.setDescription(p.getDescription());
    dto.setTotalRoom(p.getTotalRoom());
    dto.setActiveStatus(p.getActiveStatus());
    dto.setOwnerName(p.getOwnerName());
    dto.setOwnerId(p.getOwnerId() != null ? p.getOwnerId().toString() : null);
        dto.setRoomTypes(roomTypes);
        dto.setRooms(rooms);
        dto.setDeletedAt(p.getDeletedAt());
    return dto;
    }

    public static Property fromCreateRequest(PropertyCreateRequest req) {
        if (req == null) return null;
        return Property.builder()
                .propertyId(req.getPropertyId())
                .propertyName(req.getPropertyName())
                .type(req.getType())
                .address(req.getAddress())
                .province(req.getProvince())
                .description(req.getDescription())
                .totalRoom(req.getTotalRoom())
                .activeStatus(req.getActiveStatus())
                .ownerName(req.getOwnerName())
                .ownerId(UUID.fromString(req.getOwnerId()))
                .build();
    }

    public static void updateEntity(Property entity, PropertyUpdateRequest req) {
        if (entity == null || req == null) return;
        entity.setPropertyName(req.getPropertyName());
        entity.setType(req.getType());
        entity.setAddress(req.getAddress());
        entity.setProvince(req.getProvince());
        entity.setDescription(req.getDescription());
        entity.setTotalRoom(req.getTotalRoom());
        entity.setActiveStatus(req.getActiveStatus());
        entity.setOwnerName(req.getOwnerName());
        entity.setOwnerId(UUID.fromString(req.getOwnerId()));
    }

    private static RoomTypeSummaryDto toRoomTypeSummary(RoomType rt) {
        if (rt == null) return null;
        return new RoomTypeSummaryDto(rt.getRoomTypeId(), rt.getName(), rt.getPrice(), rt.getCapacity());
    }
}
