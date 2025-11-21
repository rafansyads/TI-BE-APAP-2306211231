package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerDTO;

public class CustomerMapper {
    public static CustomerDTO toDTO(Customer customer) {
        if (customer == null) return null;
        return CustomerDTO.builder()
            .id(customer.getId())
            .username(customer.getUsername())
            .name(customer.getName())
            .email(customer.getEmail())
            .saldo(customer.getSaldo())
            .build();
    }
}
