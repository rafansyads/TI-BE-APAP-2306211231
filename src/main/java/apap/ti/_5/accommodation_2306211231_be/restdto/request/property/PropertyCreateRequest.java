package apap.ti._5.accommodation_2306211231_be.restdto.request.property;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCreateRequest {
    @NotBlank private String propertyName;
    @NotNull @Min(1) private Integer type;
    @NotBlank private String address;
    @NotNull private Integer province;
    private String description;
    @NotNull @Min(0) private Integer totalRoom;
    @NotNull @Min(0) @Max(1) private Integer activeStatus;
    @NotBlank private String ownerName;
    @NotBlank private String ownerId;
}
