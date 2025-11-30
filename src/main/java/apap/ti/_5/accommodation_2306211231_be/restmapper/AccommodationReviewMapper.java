package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationReviewDTO;

public class AccommodationReviewMapper {
    public static AccommodationReviewDTO toDTO(AccommodationReview review) {
        if (review == null) return null;
        return AccommodationReviewDTO.builder()
            .reviewId(review.getReviewId())
            .propertyId(review.getProperty().getPropertyId())
            .customerUsername(review.getCustomer().getUsername())
            .bookingId(review.getBooking() != null ? review.getBooking().getBookingId() : null)
            .overallRating(review.getOverallRating())
            .cleanlinessRating(review.getCleanlinessRating())
            .facilityRating(review.getFacilityRating())
            .serviceRating(review.getServiceRating())
            .valueRating(review.getValueRating())
            .comment(review.getComment())
            .createdDate(review.getCreatedDate())
            .build();
    }
}
