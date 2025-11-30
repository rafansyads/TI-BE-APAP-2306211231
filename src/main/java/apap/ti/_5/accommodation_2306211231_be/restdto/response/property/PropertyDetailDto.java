package apap.ti._5.accommodation_2306211231_be.restdto.response.property;

import java.time.LocalDateTime;
import java.util.List;

import apap.ti._5.accommodation_2306211231_be.restdto.response.room.RoomDetailDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.room.roomtype.RoomTypeSummaryDto;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PropertyDetailDto {
    private String propertyId;
    private String propertyName;
    private Integer type;
    private String address;
    private Integer province;
    private String provinceName; // derived from province code
    private String description;
    private Integer totalRoom;
    private Integer activeStatus;
    private String ownerName;
    private String ownerId; // UUID as String for API
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime deletedAt; // soft delete marker

    // Nested aggregates
    private List<RoomTypeSummaryDto> roomTypes;
    private List<RoomDetailDto> rooms;
}
