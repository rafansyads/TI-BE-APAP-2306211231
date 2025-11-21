package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AccommodationReviewRestService {
    private final AccommodationReviewRepository reviewRepository;

    public AccommodationReviewRestService(AccommodationReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public AccommodationReview create(AccommodationReview review) {
        return reviewRepository.save(review);
    }

    public List<AccommodationReview> findByPropertyId(String propertyId) {
        return reviewRepository.findByProperty_PropertyId(propertyId);
    }

    public Optional<AccommodationReview> findById(String id) { return reviewRepository.findById(id); }

    public boolean existsForBooking(String bookingId) { return reviewRepository.existsByBooking_BookingId(bookingId); }

    public AccommodationReview update(AccommodationReview review) { return reviewRepository.save(review); }

    public void delete(String id) { reviewRepository.deleteById(id); }
}
