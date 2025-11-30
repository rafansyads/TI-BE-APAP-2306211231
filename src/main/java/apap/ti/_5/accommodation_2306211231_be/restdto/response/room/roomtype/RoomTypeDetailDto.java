package apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomTypeDetailDto {
    private String roomTypeId;
    private String name;
    private Integer price;
    private String description;
    private Integer capacity;
    private String facility;
    private Integer floor;
    private String propertyId;

    private List<RoomDetailDto> rooms;

    public void setRooms(List<RoomDetailDto> rooms) {
        this.rooms = rooms;
    }

    public List<RoomDetailDto> getRooms() {
        return rooms;
    }
}
