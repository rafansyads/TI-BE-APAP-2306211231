package apap.ti._5.accommodation_2306211231_be.restdto.response.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RegisterResponseDTO {
    private String id;
    private String username;
    private String role;
    private Instant createdAt;
}
