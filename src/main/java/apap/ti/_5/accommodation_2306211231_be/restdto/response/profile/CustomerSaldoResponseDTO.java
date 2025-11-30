package apap.ti._5.accommodation_2306211231_be.restdto.response.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSaldoResponseDTO {
    @NotBlank
    @NotNull
    private String userId;

    @NotNull
    @Min(0)
    private Long saldo;

    @NotBlank
    @NotNull
    private String username;
}
