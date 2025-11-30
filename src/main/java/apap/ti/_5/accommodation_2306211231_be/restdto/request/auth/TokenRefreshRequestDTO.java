package apap.ti._5.accommodation_2306211231_be.restdto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenRefreshRequestDTO {
    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;
}
