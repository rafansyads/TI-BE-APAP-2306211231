package apap.ti._5.accommodation_2306211231_be.restdto.response.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomSummaryDto {
    private String roomId;
    private String name;
    private Integer availabilityStatus;
    private Integer activeRoom;
    private String roomTypeId;
}
