package apap.ti._5.accommodation_2306211231_be.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accommodation_booking")
public class AccommodationBooking {

    @Id
    @Column(name = "booking_id", nullable = false, length = 64)
    @NotBlank
    private String bookingId;

    @Column(name = "check_in_date", nullable = false)
    @NotNull
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date", nullable = false)
    @NotNull
    private LocalDateTime checkOutDate;

    @Column(name = "total_days", nullable = false)
    @Min(0)
    private Integer totalDays;

    @Column(name = "total_price", nullable = false)
    @Min(0)
    private Integer totalPrice;

    // 0=pending(waiting for payment)
    // 1=payment confirmed
    // 2=canceled
    // 3=refund requested
    @Column(name = "status", nullable = false)
    @NotNull
    @Min(0)
    @Max(3)
    private Integer status;

    @Column(name = "customer_id", nullable = false)
    @NotNull
    private UUID customerId;

    @Column(name = "customer_name", nullable = false, length = 255)
    @NotBlank
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    @Email
    @NotBlank
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false, length = 50)
    @NotBlank
    private String customerPhone;

    @Column(name = "is_breakfast", nullable = false)
    @NotNull
    private Boolean isBreakfast;

    @Column(name = "refund", nullable = false)
    @Min(0)
    private Integer refund;

    @Column(name = "extra_pay", nullable = false)
    @Min(0)
    private Integer extraPay;

    @Column(name = "capacity", nullable = false)
    @Min(1)
    private Integer capacity;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonBackReference("room-bookings")
    @NotNull
    private Room room;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
