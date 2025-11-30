package apap.ti._5.accommodation_2306211231_be.restdto.request.room;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RoomUpdateRequest {
    @NotBlank private String id;
    @NotBlank private String name;
    @NotNull @Min(0) @Max(1) private Integer availabilityStatus;
    @NotNull @Min(0) @Max(1) private Integer activeRoom;
    private String maintenanceStart;
    private String maintenanceEnd;
    @NotBlank private String roomTypeId; // allow moving between room types
}
