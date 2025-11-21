package apap.ti._5.accommodation_2306211231_be.restdto.response.profile;

import lombok.*;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerDTO {
    private UUID id;
    private String username;
    private String name;
    private String email;
    private Long saldo;
}
