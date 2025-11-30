package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.repository.AccommodationReviewRepository;
import apap.ti._5.accommodation_2306211231_be.models.AccommodationReview;
import apap.ti._5.accommodation_2306211231_be.restservice.AccommodationReviewRestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccommodationReviewRestServiceUnitTest {

    @Test
    void create_delegatesToRepository() {
        var repo = Mockito.mock(AccommodationReviewRepository.class);
        var svc = new AccommodationReviewRestService(repo);
        AccommodationReview r = new AccommodationReview();
        Mockito.when(repo.save(r)).thenReturn(r);
        var out = svc.create(r);
        assertSame(r, out);
    }

    @Test
    void findByPropertyId_returnsList() {
        var repo = Mockito.mock(AccommodationReviewRepository.class);
        var svc = new AccommodationReviewRestService(repo);
        Mockito.when(repo.findByProperty_PropertyId("P1")).thenReturn(List.of(new AccommodationReview()));
        var list = svc.findByPropertyId("P1");
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
