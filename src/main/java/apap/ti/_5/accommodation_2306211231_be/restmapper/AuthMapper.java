package apap.ti._5.accommodation_2306211231_be.restmapper;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.RegisterResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LogoutResponseDTO;

import java.time.Instant;
import java.util.List;

public final class AuthMapper {
    private AuthMapper() {}

    public static LoginResponseDTO toLoginResponseDto(EndUser user, List<String> roles, String token, Instant expiresAt) {
        if (user == null) return null;
        return LoginResponseDTO.builder()
                .id(user.getId() == null ? null : user.getId().toString())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .roles(roles)
                .token(token)
                .expiresAt(expiresAt)
                .build();
    }

    public static RegisterResponseDTO toRegisterResponseDto(EndUser created, String role, Instant createdAt) {
        if (created == null) return null;
        return RegisterResponseDTO.builder()
                .id(created.getId() == null ? null : created.getId().toString())
                .username(created.getUsername())
                .role(role)
                .createdAt(createdAt)
                .build();
    }

    public static LogoutResponseDTO toLogoutResponseDto(String message, Instant timestamp) {
        return LogoutResponseDTO.builder()
                .message(message)
                .timestamp(timestamp)
                .build();
    }
}
