package apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking;

import lombok.*;
import java.time.LocalDateTime;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class AccommodationReviewDTO {
    private String reviewId;
    private String propertyId;
    private String customerUsername;
    private String bookingId;
    private Integer overallRating;
    private Integer cleanlinessRating;
    private Integer facilityRating;
    private Integer serviceRating;
    private Integer valueRating;
    private String comment;
    private LocalDateTime createdDate;
}
