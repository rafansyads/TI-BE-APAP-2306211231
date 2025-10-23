package apap.ti._5.accommodation_2306211231_be.restdto.response.room;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RoomDetailDto {
    private String roomId;
    private String name;
    private Integer availabilityStatus;
    private Integer activeRoom;
    private String maintenanceStart; // ISO string
    private String maintenanceEnd;
    private String roomTypeId;
}
