package apap.ti._5.accommodation_2306211231_be.restservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.models.Property;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.repository.AccommodationReviewRepository;

class AccommodationReviewRestServiceTests {

    @Mock
    private AccommodationReviewRepository reviewRepository;

    private AccommodationReviewRestService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AccommodationReviewRestService(reviewRepository);
    }

    @Test
    void create_success_returnsReview() {
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("rev-1");
        review.setOverallRating(5);
        when(reviewRepository.save(review)).thenReturn(review);

        AccommodationReview result = service.create(review);

        assertNotNull(result);
        assertEquals("rev-1", result.getReviewId());
        verify(reviewRepository).save(review);
    }

    @Test
    void findByPropertyId_returnsReviews() {
        Property property = new Property();
        property.setPropertyId("prop-1");
        
        AccommodationReview r1 = new AccommodationReview();
        r1.setReviewId("rev-1");
        r1.setProperty(property);
        AccommodationReview r2 = new AccommodationReview();
        r2.setReviewId("rev-2");
        r2.setProperty(property);
        
        when(reviewRepository.findByProperty_PropertyId("prop-1")).thenReturn(List.of(r1, r2));

        List<AccommodationReview> result = service.findByPropertyId("prop-1");

        assertEquals(2, result.size());
    }

    @Test
    void findByPropertyId_emptyList() {
        when(reviewRepository.findByProperty_PropertyId("unknown")).thenReturn(List.of());

        List<AccommodationReview> result = service.findByPropertyId("unknown");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCustomerId_returnsReviews() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        
        AccommodationReview r1 = new AccommodationReview();
        r1.setReviewId("rev-1");
        r1.setCustomer(customer);
        
        when(reviewRepository.findByCustomer_Id(customerId)).thenReturn(List.of(r1));

        List<AccommodationReview> result = service.findByCustomerId(customerId);

        assertEquals(1, result.size());
    }

    @Test
    void findByCustomerId_emptyList() {
        UUID customerId = UUID.randomUUID();
        when(reviewRepository.findByCustomer_Id(customerId)).thenReturn(List.of());

        List<AccommodationReview> result = service.findByCustomerId(customerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void findById_exists_returnsOptionalWithReview() {
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("rev-1");
        when(reviewRepository.findById("rev-1")).thenReturn(Optional.of(review));

        Optional<AccommodationReview> result = service.findById("rev-1");

        assertTrue(result.isPresent());
        assertEquals("rev-1", result.get().getReviewId());
    }

    @Test
    void findById_notExists_returnsEmptyOptional() {
        when(reviewRepository.findById("unknown")).thenReturn(Optional.empty());

        Optional<AccommodationReview> result = service.findById("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void existsForBooking_exists_returnsTrue() {
        when(reviewRepository.existsByBooking_BookingId("book-1")).thenReturn(true);

        boolean result = service.existsForBooking("book-1");

        assertTrue(result);
    }

    @Test
    void existsForBooking_notExists_returnsFalse() {
        when(reviewRepository.existsByBooking_BookingId("book-1")).thenReturn(false);

        boolean result = service.existsForBooking("book-1");

        assertFalse(result);
    }

    @Test
    void update_success_returnsUpdatedReview() {
        AccommodationReview review = new AccommodationReview();
        review.setReviewId("rev-1");
        review.setOverallRating(4);
        when(reviewRepository.save(review)).thenReturn(review);

        AccommodationReview result = service.update(review);

        assertNotNull(result);
        assertEquals(4, result.getOverallRating());
    }

    @Test
    void delete_callsDeleteById() {
        service.delete("rev-1");

        verify(reviewRepository).deleteById("rev-1");
    }
}
