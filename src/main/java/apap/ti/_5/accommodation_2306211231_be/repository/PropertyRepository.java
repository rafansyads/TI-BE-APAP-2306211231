package apap.ti._5.accommodation_2306211231_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import apap.ti._5.accommodation_2306211231_be.models.Property;

public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByDeletedAtIsNull();
    Optional<Property> findByPropertyIdAndDeletedAtIsNull(String propertyId);
    long countByDeletedAtIsNull();
}
