package apap.ti._5.accommodation_2306211231_be.restdto.response.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class LoginResponseDTO {
    private String id;
    private String username;
    private String name;
    private String email;
    private List<String> roles;
    private String token;
    private Instant expiresAt;
}
