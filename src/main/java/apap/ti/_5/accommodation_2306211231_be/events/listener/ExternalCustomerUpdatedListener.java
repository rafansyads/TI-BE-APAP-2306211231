package apap.ti._5.accommodation_2306211231_be.events.listener;

import apap.ti._5.accommodation_2306211231_be.events.ExternalCustomerUpdatedEvent;
import apap.ti._5.accommodation_2306211231_be.repository.profile.CustomerRepository;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ExternalCustomerUpdatedListener {
    private final CustomerRepository customerRepository;

    public ExternalCustomerUpdatedListener(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @EventListener
    public void handle(ExternalCustomerUpdatedEvent event) {
        customerRepository.findById(event.id()).ifPresent(customer -> {
            customer.setUsername(event.username());
            customer.setName(event.name());
            customer.setEmail(event.email());
            customerRepository.save(customer);
        });
    }
}
