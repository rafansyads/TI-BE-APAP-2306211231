package apap.ti._5.accommodation_2306211231_be.repository.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.Superadmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SuperadminRepository extends JpaRepository<Superadmin, UUID> {
    Optional<Superadmin> findByUsername(String username);
    Optional<Superadmin> findByEmail(String email);
    Optional<Superadmin> findByEmailIgnoreCase(String email);
}
