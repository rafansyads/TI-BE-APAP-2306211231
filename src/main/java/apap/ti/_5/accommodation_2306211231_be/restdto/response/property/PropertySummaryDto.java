package apap.ti._5.accommodation_2306211231_be.restdto.response.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySummaryDto {
    private String propertyId;
    private String propertyName;
    private Integer type;
    private Integer province;
    private String provinceName; // derived from province code
    private Integer activeStatus;
    private Integer totalRoom;
}
