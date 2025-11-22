package apap.ti._5.accommodation_2306211231_be.repository.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.TourPackageVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourPackageVendorRepository extends JpaRepository<TourPackageVendor, UUID> {
    Optional<TourPackageVendor> findByUsername(String username);
    Optional<TourPackageVendor> findByEmail(String email);
    Optional<TourPackageVendor> findByEmailIgnoreCase(String email);
}
