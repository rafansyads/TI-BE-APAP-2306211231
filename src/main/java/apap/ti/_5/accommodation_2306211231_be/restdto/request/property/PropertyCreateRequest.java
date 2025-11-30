package apap.ti._5.accommodation_2306211231_be.restdto.request.property;

import java.util.List;

import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeCreateRequest;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCreateRequest {
    @NotBlank private String propertyName;
    @NotNull @Min(1) private Integer type;
    @NotBlank private String address;
    @NotNull private Integer province;
    private String description;
    @NotNull @Min(0) private Integer totalRoom;
    @NotNull @Min(0) @Max(1) private Integer activeStatus;
    @NotBlank private String ownerName;
    @NotBlank private String ownerId;

    // Nested payloads for initial inventory creation.
    // Note: We intentionally do NOT add @Valid here to avoid forcing clients to supply IDs we will generate server-side.
    private List<RoomTypeCreateRequest> roomTypes;
}
