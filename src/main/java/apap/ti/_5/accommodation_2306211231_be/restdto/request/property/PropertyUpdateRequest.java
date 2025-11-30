package apap.ti._5.accommodation_2306211231_be.restdto.request.property;

import java.util.List;

import apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype.RoomTypeUpdateRequest;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyUpdateRequest {
    @NotBlank private String propertyId;
    @NotBlank private String propertyName;
    // present for backward compatibility in tests; ignored by service
    private Integer type;
    @NotBlank private String address;
    private Integer province; // optional, but if provided must be valid code
    private String description;
    // present for backward compatibility in tests; ignored by service
    private Integer totalRoom;
    private Integer activeStatus;
    @NotBlank private String ownerName;
    @NotBlank private String ownerId;

    private List<RoomTypeUpdateRequest> roomTypes;
}
