package apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype;

import java.util.List;

import apap.ti._5.accommodation_2306211231_be.restdto.request.room.RoomCreateRequest;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeCreateRequest {
    // Optional: client may omit; when provided, must match generated ID
    private String roomTypeId; // optional client-provided linking key
    @NotBlank private String name;
    @NotNull @Min(0) private Integer price;
    private String description;
    @NotNull @Min(1) private Integer capacity;
    private String facility;
    @NotNull @Min(0) private Integer floor;
    // Not required in create since backend assigns property
    private String propertyId; // ignored on create
    // Nested rooms to be created under this room type
    private List<RoomCreateRequest> rooms;
}
