package apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeSummaryDto {
    private String roomTypeId;
    private String name;
    private Integer price;
    private Integer capacity;
}
