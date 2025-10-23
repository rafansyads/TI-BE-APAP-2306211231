package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.RoomType;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomSummaryDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeSummaryDto;

import java.util.List;
import java.util.stream.Collectors;

public final class RoomTypeMapper {
    private RoomTypeMapper() {}

    public static RoomTypeSummaryDto toSummaryDto(RoomType rt) {
        if (rt == null) return null;
        return new RoomTypeSummaryDto(rt.getRoomTypeId(), rt.getName(), rt.getPrice(), rt.getCapacity());
    }

    public static RoomTypeDetailDto toDetailDto(RoomType rt) {
        if (rt == null) return null;
        RoomTypeDetailDto dto = new RoomTypeDetailDto();
        dto.setRoomTypeId(rt.getRoomTypeId());
        dto.setName(rt.getName());
        dto.setPrice(rt.getPrice());
        dto.setDescription(rt.getDescription());
        dto.setCapacity(rt.getCapacity());
        dto.setFacility(rt.getFacility());
        dto.setFloor(rt.getFloor());
        dto.setPropertyId(rt.getProperty() != null ? rt.getProperty().getPropertyId() : null);
        if (rt.getListRoom() != null) {
            List<RoomSummaryDto> rooms = rt.getListRoom().stream()
                    .map(RoomMapper::toSummaryDto)
                    .collect(Collectors.toList());
            dto.setRooms(rooms);
        }
        return dto;
    }

    public static RoomType fromCreateRequest(RoomTypeCreateRequest req) {
        if (req == null) return null;
        RoomType rt = new RoomType();
        rt.setRoomTypeId(req.getRoomTypeId());
        rt.setName(req.getName());
        rt.setPrice(req.getPrice());
        rt.setDescription(req.getDescription());
        rt.setCapacity(req.getCapacity());
        rt.setFacility(req.getFacility());
        rt.setFloor(req.getFloor());
        // property relationship should be set in service using propertyId
        return rt;
    }

    public static void updateEntity(RoomType rt, RoomTypeUpdateRequest req) {
        if (rt == null || req == null) return;
        rt.setName(req.getName());
        rt.setPrice(req.getPrice());
        rt.setDescription(req.getDescription());
        rt.setCapacity(req.getCapacity());
        rt.setFacility(req.getFacility());
        rt.setFloor(req.getFloor());
    }
}
