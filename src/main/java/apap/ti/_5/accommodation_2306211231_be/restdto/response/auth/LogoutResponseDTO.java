package apap.ti._5.accommodation_2306211231_be.restdto.response.auth;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LogoutResponseDTO {
    private String message;
    private Instant timestamp;
}
