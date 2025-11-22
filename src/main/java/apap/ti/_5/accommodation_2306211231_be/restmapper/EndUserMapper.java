package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;

public final class EndUserMapper {
    private EndUserMapper() {}

    public static EndUserResponseDTO toDTO(EndUser u, String role) {
        if (u == null) return null;
        return EndUserResponseDTO.builder()
                .id(u.getId() == null ? null : u.getId().toString())
                .username(u.getUsername())
                .name(u.getName())
                .email(u.getEmail())
                .gender(u.getGender())
                .createdAt(u.getCreatedAt())
                .role(role)
                .build();
    }
}
