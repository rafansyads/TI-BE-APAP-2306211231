package apap.ti._5.accommodation_2306211231_be.repository.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.InsuranceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceProviderRepository extends JpaRepository<InsuranceProvider, UUID> {
    Optional<InsuranceProvider> findByUsername(String username);
}
