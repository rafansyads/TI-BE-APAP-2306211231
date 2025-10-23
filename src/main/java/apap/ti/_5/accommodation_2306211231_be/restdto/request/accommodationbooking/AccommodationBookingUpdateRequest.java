package apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationBookingUpdateRequest {
    @NotBlank private String checkInDate;  // ISO strings for API
    @NotBlank private String checkOutDate;
    @NotNull @Min(0) private Integer totalDays;
    @NotNull @Min(0) private Integer totalPrice;
    @NotNull @Min(0) @Max(3) private Integer status;
    @NotBlank private String customerName;
    @Email @NotBlank private String customerEmail;
    @NotBlank private String customerPhone;
    @NotNull private Boolean isBreakfast;
    @NotNull @Min(0) private Integer refund;
    @NotNull @Min(0) private Integer extraPay;
    @NotNull @Min(1) private Integer capacity;
    @NotBlank private String roomId;
}
