package apap.ti._5.accommodation_2306211231_be.restdto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndUserResponseDTO {
    private String id;
    private String username;
    private String name;
    private String email;
    private Boolean gender;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long saldo;
    private String role;
}
