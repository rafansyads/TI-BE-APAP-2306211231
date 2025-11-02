package apap.ti._5.accommodation_2306211231_be.restdto.request.room;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {
    // Optional: client may omit; when provided, must match generated ID
    private String roomId;
    // Name will be auto-generated from room number (e.g., 202)
    private String name;
    @NotNull @Min(0) @Max(1) private Integer availabilityStatus;
    @NotNull @Min(0) @Max(1) private Integer activeRoom;
    private String maintenanceStart; // ISO string; convert in service
    private String maintenanceEnd;
    // Optional: if multiple room types are defined, client must provide; if exactly one, may omit
    private String roomTypeId;
}
