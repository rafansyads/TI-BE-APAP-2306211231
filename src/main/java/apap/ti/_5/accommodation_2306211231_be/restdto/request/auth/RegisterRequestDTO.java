package apap.ti._5.accommodation_2306211231_be.restdto.request.auth;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class RegisterRequestDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotNull // true male, false female
    private Boolean gender;

    // SUPERADMIN is forbidden; when absent backend defaults to CUSTOMER
    // role left optional to allow clients to omit it and let backend default to CUSTOMER
    private String role;

    // rental vendor only (optional)
    private String phone;

    // rental vendor only (optional)
    private List<String> locations;
}
