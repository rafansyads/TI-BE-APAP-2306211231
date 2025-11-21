package apap.ti._5.accommodation_2306211231_be.repository;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccommodationReviewRepository extends JpaRepository<AccommodationReview, String> {
    List<AccommodationReview> findByProperty_PropertyId(String propertyId);
    boolean existsByBooking_BookingId(String bookingId);
}
