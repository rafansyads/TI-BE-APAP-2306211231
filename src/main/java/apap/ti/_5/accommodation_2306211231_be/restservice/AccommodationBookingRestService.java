package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationBooking;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationBookingRepository;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingCreateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.request.accommodationbooking.AccommodationBookingUpdateRequest;
import apap.ti._5.accommodation_2306211231_be.restdto.response.accommodationbooking.AccommodationBookingDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccommodationBookingRestService {

    private final AccommodationBookingRepository bookingRepository;

    public long count() {
        return bookingRepository.count();
    }

    // Placeholders to be implemented later (entity-based)
    public List<AccommodationBooking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<AccommodationBooking> getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public AccommodationBooking createBooking(AccommodationBooking booking) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AccommodationBooking updateBooking(String bookingId, AccommodationBooking booking) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void deleteBooking(String bookingId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // DTO-based method signatures for controllers (stubs)
    public List<AccommodationBookingDto> getAllBookingsDto() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AccommodationBookingDto getBookingDtoById(String bookingId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AccommodationBookingDto createBooking(AccommodationBookingCreateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public AccommodationBookingDto updateBooking(String bookingId, AccommodationBookingUpdateRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
