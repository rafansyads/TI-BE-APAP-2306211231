package apap.ti._5.accommodation_2306211231_be.restmapper;

import java.time.LocalDateTime;

import apap.ti._5.accommodation_2306211231_be.models.Room;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomSummaryDto;

public final class RoomMapper {
    private RoomMapper() {}

    public static RoomSummaryDto toSummaryDto(Room r) {
        if (r == null) return null;
        return new RoomSummaryDto(
                r.getRoomId(),
                r.getName(),
                r.getAvailabilityStatus(),
                r.getActiveRoom(),
                r.getRoomType() != null ? r.getRoomType().getRoomTypeId() : null
        );
    }

    public static RoomDetailDto toDetailDto(Room r) {
        if (r == null) return null;
        RoomDetailDto dto = new RoomDetailDto();
        dto.setRoomId(r.getRoomId());
        dto.setName(r.getName());
        // compute availability considering maintenance window timing
        Integer computedAvailability = r.getAvailabilityStatus();
        if (r.getMaintenanceStart() != null && r.getMaintenanceEnd() != null) {
            var now = java.time.LocalDateTime.now();
            var start = r.getMaintenanceStart();
            var end = r.getMaintenanceEnd();
            // Inclusive at start, exclusive at end to match 1->0 at start and 0->1 at end
            boolean inMaintenance = (now.isEqual(start) || now.isAfter(start)) && now.isBefore(end);
            if (inMaintenance) {
                computedAvailability = 0; // force unavailable during maintenance
            } else if (now.isEqual(end) || now.isAfter(end)) {
                // maintenance finished – if previously switched off due to maintenance, expose available again
                if (computedAvailability != null && computedAvailability == 0) {
                    computedAvailability = 1;
                }
            }
        }
        dto.setAvailabilityStatus(computedAvailability);
        dto.setActiveRoom(r.getActiveRoom());
        dto.setMaintenanceStart(r.getMaintenanceStart() != null ? r.getMaintenanceStart().toString() : null);
        dto.setMaintenanceEnd(r.getMaintenanceEnd() != null ? r.getMaintenanceEnd().toString() : null);
        dto.setRoomTypeId(r.getRoomType() != null ? r.getRoomType().getRoomTypeId() : null);
        return dto;
    }

    public static Room fromCreateRequest(RoomCreateRequest req) {
        if (req == null) return null;
        Room r = new Room();
        r.setRoomId(req.getRoomId());
        r.setName(req.getName());
        r.setAvailabilityStatus(req.getAvailabilityStatus());
        r.setActiveRoom(req.getActiveRoom());
        r.setMaintenanceStart(parseDate(req.getMaintenanceStart()));
        r.setMaintenanceEnd(parseDate(req.getMaintenanceEnd()));
        // roomType relation to be set in service
        return r;
    }

    public static void updateEntity(Room r, RoomUpdateRequest req) {
        if (r == null || req == null) return;
        r.setName(req.getName());
        r.setAvailabilityStatus(req.getAvailabilityStatus());
        r.setActiveRoom(req.getActiveRoom());
        r.setMaintenanceStart(parseDate(req.getMaintenanceStart()));
        r.setMaintenanceEnd(parseDate(req.getMaintenanceEnd()));
        // roomType relation to be updated in service if needed
    }

    private static LocalDateTime parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        return LocalDateTime.parse(s);
    }
}
