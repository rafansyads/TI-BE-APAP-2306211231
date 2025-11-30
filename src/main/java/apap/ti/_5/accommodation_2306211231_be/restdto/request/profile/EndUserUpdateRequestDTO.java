package apap.ti._5.accommodation_2306211231_be.restdto.request.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndUserUpdateRequestDTO {
    private String username;
    private String name;
    private String password;
    private String email;
    private Boolean gender;
    // saldo is allowed to be updated only by SUPERADMIN
    private Long saldo;
}
