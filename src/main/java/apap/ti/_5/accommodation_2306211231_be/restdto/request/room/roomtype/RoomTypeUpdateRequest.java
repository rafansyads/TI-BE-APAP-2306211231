package apap.ti._5.accommodation_2306211231_be.restdto.request.room.roomtype;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeUpdateRequest {
    @NotBlank
    private String roomTypeId; // required to identify which RoomType to update
    private String name; // optional, ignored in Property-level update
    @NotNull @Min(0) private Integer price;
    private String description;
    @NotNull @Min(1) private Integer capacity;
    private String facility;
    @NotNull @Min(0) private Integer floor; // ignored in Property-level update
}
