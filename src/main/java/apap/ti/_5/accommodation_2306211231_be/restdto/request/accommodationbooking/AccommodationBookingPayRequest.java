package apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationBookingPayRequest {
    @NotBlank private String bookingId;
    @NotNull @Min(1) @Max(1) private Integer status;
    @Min(0) private Integer extraPay;
}
