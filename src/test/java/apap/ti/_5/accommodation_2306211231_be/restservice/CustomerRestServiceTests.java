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

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;

class CustomerRestServiceTests {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerRestService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CustomerRestService(customerRepository);
    }

    @Test
    void create_success_returnsCustomer() {
        Customer customer = new Customer();
        customer.setUsername("newcust");
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = service.create(customer);

        assertNotNull(result);
        assertEquals("newcust", result.getUsername());
        verify(customerRepository).save(customer);
    }

    @Test
    void findById_exists_returnsOptionalWithCustomer() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = service.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_notExists_returnsEmptyOptional() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Customer> result = service.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void findByUsername_exists_returnsOptionalWithCustomer() {
        Customer customer = new Customer();
        customer.setUsername("testuser");
        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(customer));

        Optional<Customer> result = service.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_notExists_returnsEmptyOptional() {
        when(customerRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Optional<Customer> result = service.findByUsername("unknown");

        assertFalse(result.isPresent());
    }

    @Test
    void findAll_returnsAllCustomers() {
        Customer c1 = new Customer();
        c1.setUsername("cust1");
        Customer c2 = new Customer();
        c2.setUsername("cust2");
        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = service.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        List<Customer> result = service.findAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void update_success_returnsUpdatedCustomer() {
        Customer customer = new Customer();
        customer.setUsername("updated");
        when(customerRepository.save(customer)).thenReturn(customer);

        Customer result = service.update(customer);

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
    }

    @Test
    void delete_callsDeleteById() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(customerRepository).deleteById(id);
    }
}
