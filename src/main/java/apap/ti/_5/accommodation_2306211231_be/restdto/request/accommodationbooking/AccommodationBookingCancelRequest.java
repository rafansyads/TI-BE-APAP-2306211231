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
public class AccommodationBookingCancelRequest {
    @NotBlank private String bookingId;
    @NotNull @Min(2) @Max(2) private Integer status;
    @NotNull @Min(0) private Integer refund;
    @NotNull @Min(0) private Integer extraPay;
}
