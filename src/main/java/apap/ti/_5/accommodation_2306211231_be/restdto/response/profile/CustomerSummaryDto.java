package apap.ti._5.accommodation_2306211231_be.restdto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSummaryDto {
    private String customerId; // UUID as string
    private String customerName;
    private String customerEmail;
    private String customerPhone;
}
