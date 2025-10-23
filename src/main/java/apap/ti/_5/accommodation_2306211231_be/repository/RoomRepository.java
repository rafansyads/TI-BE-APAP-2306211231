package apap.ti._5.accommodation_2306211231_be.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import apap.ti._5.accommodation_2306211231_be.models.Room;

public interface RoomRepository extends JpaRepository<Room, String> {
    // Define query methods here
}
