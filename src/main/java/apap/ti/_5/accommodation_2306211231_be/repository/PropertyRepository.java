package apap.ti._5.accommodation_2306211231_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, String> {
    List<Property> findByDeletedAtIsNull();
    Optional<Property> findByPropertyIdAndDeletedAtIsNull(String propertyId);
    long countByDeletedAtIsNull();

    // Owner-based lookups to validate UUID-name consistency across properties
    Optional<Property> findFirstByOwnerId(UUID ownerId);

    // For validating owner name and ID consistency
    Optional<Property> findFirstByOwnerNameAndOwnerId(String ownerName, UUID ownerId);

    
}
