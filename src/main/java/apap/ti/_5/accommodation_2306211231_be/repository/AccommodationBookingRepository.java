package apap.ti._5.accommodation_2306211231_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;

public interface AccommodationBookingRepository extends JpaRepository<AccommodationBooking, String> {
    // Define query methods here
}
