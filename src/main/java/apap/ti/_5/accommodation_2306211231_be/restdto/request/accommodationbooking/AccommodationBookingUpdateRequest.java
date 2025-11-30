package apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccommodationBookingUpdateRequest {
    @NotBlank private String bookingId; // provided by client
    @NotNull private LocalDateTime checkInDate;  // ISO LocalDateTime from API
    @NotNull private LocalDateTime checkOutDate;
    @NotNull @Min(0) private Integer totalDays;
    @NotNull @Min(0) private Integer totalPrice;
    @NotNull @Min(0) @Max(3) private Integer status;
    @NotBlank private String customerId; // UUID as string, must match existing
    @NotBlank private String customerName;
    @Email @NotBlank private String customerEmail;
    @NotBlank private String customerPhone;
    @NotNull private Boolean isBreakfast;
    @NotNull @Min(0) private Integer refund;
    @NotNull @Min(0) private Integer extraPay;
    @NotNull @Min(1) private Integer capacity;
    @NotBlank private String roomId;

    // Additional fields from Property, Room Type, and Room for easier reference
    @NotBlank private String propertyName;
    @NotBlank private String roomTypeName;
    @NotBlank private String roomName;
}
