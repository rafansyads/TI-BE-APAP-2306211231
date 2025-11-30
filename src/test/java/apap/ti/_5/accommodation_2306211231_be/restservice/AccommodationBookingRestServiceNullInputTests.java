package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.repository.RoomRepository;
import apap.ti._5.accommodation_2306211231_be.repository.PropertyRepository;
import org.mockito.Mockito;

class AccommodationBookingRestServiceNullInputTests {

    @Test
    void getBookingDtoById_nullId_throwsIllegalArgument() {
        var service = new AccommodationBookingRestService(
            Mockito.mock(AccommodationBookingRepository.class),
            Mockito.mock(RoomRepository.class),
            Mockito.mock(PropertyRepository.class),
            Mockito.mock(apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService.class)
        );
        assertThrows(IllegalArgumentException.class, () -> service.getBookingDtoById(null));
    }
}
