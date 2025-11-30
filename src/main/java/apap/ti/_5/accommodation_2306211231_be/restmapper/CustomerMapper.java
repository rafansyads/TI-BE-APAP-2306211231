package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.AccommodationCustomerResponseDTO;

public class CustomerMapper {
    public static AccommodationCustomerResponseDTO toDTO(Customer customer) {
        if (customer == null) return null;
        return AccommodationCustomerResponseDTO.builder()
            .id(customer.getId())
            .username(customer.getUsername())
            .name(customer.getName())
            .email(customer.getEmail())
            .saldo(customer.getSaldo())
            .build();
    }

    public static apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO toCustomerResponseDto(Customer customer) {
        if (customer == null) return null;
        java.util.List<String> reviewIds = customer.getReviews() == null ? java.util.List.of() : customer.getReviews().stream().map(r -> r.getReviewId()).toList();
        return apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO.builder()
                .id(customer.getId() == null ? null : customer.getId().toString())
                .username(customer.getUsername())
                .name(customer.getName())
                .email(customer.getEmail())
                .gender(customer.getGender())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .isDeleted(customer.getIsDeleted())
                .saldo(customer.getSaldo())
                .reviewIds(reviewIds)
                .build();
    }
}
