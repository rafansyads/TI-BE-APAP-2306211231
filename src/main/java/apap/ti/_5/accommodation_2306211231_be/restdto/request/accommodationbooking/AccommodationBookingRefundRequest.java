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
public class AccommodationBookingRefundRequest {
    @NotBlank private String bookingId;
    @NotNull @Positive private Integer refund;
}
