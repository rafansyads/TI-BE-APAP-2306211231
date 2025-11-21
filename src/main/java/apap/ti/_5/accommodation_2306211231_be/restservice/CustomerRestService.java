package apap.ti._5.accommodation_2306211231_be.restservice;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CustomerRestService {
    private final CustomerRepository customerRepository;

    public CustomerRestService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> findById(UUID id) { return customerRepository.findById(id); }
    public Optional<Customer> findByUsername(String username) { return customerRepository.findByUsername(username); }
    public List<Customer> findAll() { return customerRepository.findAll(); }

    public Customer update(Customer customer) { return customerRepository.save(customer); }

    public void delete(UUID id) { customerRepository.deleteById(id); }
}
