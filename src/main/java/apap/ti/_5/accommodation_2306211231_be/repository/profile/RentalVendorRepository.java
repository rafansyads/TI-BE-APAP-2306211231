package apap.ti._5.accommodation_2306211231_be.repository.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.RentalVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RentalVendorRepository extends JpaRepository<RentalVendor, UUID> {
    Optional<RentalVendor> findByUsername(String username);
}
