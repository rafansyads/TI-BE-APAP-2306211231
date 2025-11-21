package apap.ti._5.accommodation_2306211231_be.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "accommodation_review")
public class AccommodationReview {

    @Id
    @Column(name = "review_id", nullable = false, length = 64)
    @NotBlank
    private String reviewId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonBackReference("property-reviews")
    @NotNull
    private Property property;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    @JsonBackReference("customer-reviews")
    @NotNull
    private Customer customer;

    // Each review is associated with exactly one booking; a booking may or may not have a review.
    @OneToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    @JsonBackReference("booking-review")
    private AccommodationBooking booking;

    @Column(name = "overall_rating", nullable = false)
    @Min(0)
    @Max(10)
    private Integer overallRating;

    @Column(name = "cleanliness_rating", nullable = false)
    @Min(0)
    @Max(10)
    private Integer cleanlinessRating;

    @Column(name = "facility_rating", nullable = false)
    @Min(0)
    @Max(10)
    private Integer facilityRating;

    @Column(name = "service_rating", nullable = false)
    @Min(0)
    @Max(10)
    private Integer serviceRating;

    @Column(name = "value_rating", nullable = false)
    @Min(0)
    @Max(10)
    private Integer valueRating;

    @Column(name = "comment", columnDefinition = "text")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;
}
