package apap.ti._5.accommodation_2306211231_be.restdto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TokenRefreshRequestDTO {
    private String token;
    private String username;
    private String email;
}
