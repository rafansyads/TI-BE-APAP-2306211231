package apap.ti._5.accommodation_2306211231_be.repository.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
