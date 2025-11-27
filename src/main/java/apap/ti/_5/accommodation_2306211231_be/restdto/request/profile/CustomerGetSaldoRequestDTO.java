package apap.ti._5.accommodation_2306211231_be.restdto.request.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerGetSaldoRequestDTO {
    @NotBlank
    @NotNull
    private String userId;

    @NotBlank
    @NotNull
    private String username;
}
