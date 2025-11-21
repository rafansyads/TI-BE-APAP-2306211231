package apap.ti._5.accommodation_2306211231_be.restdto.request.auth;

import lombok.Data;

import java.util.List;

@Data
public class RegisterRequestDTO {
    private String username;
    private String password;
    private String name;
    private String email;
    private Boolean gender; // true male, false female
    private String role; // e.g. CUSTOMER, RENTAL_VENDOR, FLIGHT_AIRLINE, INSURANCE_PROVIDER, TOUR_PACKAGE_VENDOR (SUPERADMIN forbidden)
    private String phone; // rental vendor only (optional)
    private List<String> locations; // rental vendor only (optional)
}
