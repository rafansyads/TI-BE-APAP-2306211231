package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restservice.CustomerRestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerRestServiceUnitTest {

    @Test
    void create_callsRepositorySave() {
        var repo = Mockito.mock(CustomerRepository.class);
        var svc = new CustomerRestService(repo);
        Customer c = new Customer();
        c.setUsername("joe");
        Mockito.when(repo.save(c)).thenReturn(c);
        var out = svc.create(c);
        assertSame(c, out);
    }

    @Test
    void findByUsername_returnsOptional() {
        var repo = Mockito.mock(CustomerRepository.class);
        var svc = new CustomerRestService(repo);
        Customer c = new Customer();
        c.setUsername("joe");
        Mockito.when(repo.findByUsername("joe")).thenReturn(Optional.of(c));
        var opt = svc.findByUsername("joe");
        assertTrue(opt.isPresent());
        assertEquals("joe", opt.get().getUsername());
    }
}
