package apap.ti._5.accommodation_2306211231_be.restdto.request.room;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomCreateRequest {
    @NotBlank private String roomId; // provided by client
    @NotBlank private String name;
    @NotNull @Min(0) @Max(1) private Integer availabilityStatus;
    @NotNull @Min(0) @Max(1) private Integer activeRoom;
    private String maintenanceStart; // ISO string; convert in service
    private String maintenanceEnd;
    @NotBlank private String roomTypeId;
}
