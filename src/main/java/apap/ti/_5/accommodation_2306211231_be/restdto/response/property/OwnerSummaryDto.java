package apap.ti._5.accommodation_2306211231_be.restdto.response.property;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OwnerSummaryDto {
    private String ownerId; // UUID as string
    private String ownerName;
}
