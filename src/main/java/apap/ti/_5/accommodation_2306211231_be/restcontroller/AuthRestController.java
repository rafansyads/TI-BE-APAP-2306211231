package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.LoginRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LoginResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.LogoutResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.auth.RegisterResponseDTO;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import lombok.RequiredArgsConstructor;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthRestService authRestService;

    // test method with get mapping
    @GetMapping
    public void test() {
        System.out.println("======== AuthRestController CONSTRUCTOR CALLED ========");
        System.out.println("AuthenticationManager: " + authenticationManager);
        System.out.println("JwtUtils: " + jwtUtils);
        System.out.println("AuthRestService: " + authRestService);        System.out.println("========================================");
        System.out.println("AuthRestController BEAN CREATED!");
    }

    @PostMapping("/login")
    public ResponseEntity<BaseResponseDto<LoginResponseDTO>> login(
        @RequestBody BaseRequestDto<LoginRequestDTO> request) {
        try {
            if (request.getData() == null) {
                return ResponseEntity.badRequest().body(errorResponse(HttpStatus.BAD_REQUEST, "Missing data object"));
            }
            LoginRequestDTO payload = request.getData();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(payload.getUsername(), payload.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Resolve user & role for JWT claims
            EndUser user = authRestService.findAggregateByUsername(payload.getUsername());
            List<String> roles = authRestService.resolveRoles(user);

            String token = jwtUtils.generateJwtToken(user.getId(), user.getUsername(), user.getEmail(), user.getName(), roles);
            LoginResponseDTO data = LoginResponseDTO.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .name(user.getName())
                    .email(user.getEmail())
                    .roles(roles)
                    .token(token)
                    .expiresAt(Instant.now().plusMillis(jwtUtils.getJwtExpirationMs()))
                    .build();
            return ResponseEntity.ok(successResponse(data, "Login success"));
        } catch (BadCredentialsException ex){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<BaseResponseDto<RegisterResponseDTO>> register(
        @RequestBody BaseRequestDto<RegisterRequestDTO> request) {
        if (request.getData() == null) {
            return ResponseEntity.badRequest().body(errorResponse(HttpStatus.BAD_REQUEST, "Missing data object"));
        }
        RegisterRequestDTO payload = request.getData();
        if ("SUPERADMIN".equalsIgnoreCase(payload.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse(HttpStatus.FORBIDDEN, "Cannot self-register SUPERADMIN role"));
        }
        if (authRestService.existsUsername(payload.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse(HttpStatus.CONFLICT, "Username already taken"));
        }
        if (authRestService.existsEmail(payload.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse(HttpStatus.CONFLICT, "Email already taken"));
        }
        EndUser created = authRestService.register(payload);

        RegisterResponseDTO data = RegisterResponseDTO.builder()
                .id(created.getId().toString())
                .username(created.getUsername())
                .role(authRestService.resolveRoles(created).get(0))
                .createdAt(Instant.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(successResponse(data, "Register success"));
    }

    @PostMapping("/logout")
    public ResponseEntity<BaseResponseDto<LogoutResponseDTO>> logout() {
        SecurityContextHolder.clearContext();
        LogoutResponseDTO data = LogoutResponseDTO.builder()
                .message("Logged out (client must discard token)")
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(successResponse(data, "Logout success"));
    }

    // Controller now delegates user/role/existence logic to AuthRestService

    private <T> BaseResponseDto<T> successResponse(T data, String message) {
        BaseResponseDto<T> resp = new BaseResponseDto<>();
        resp.setStatus(HttpStatus.OK.value());
        resp.setMessage(message);
        resp.setTimestamp(new java.util.Date());
        resp.setData(data);
        return resp;
    }

    private <T> BaseResponseDto<T> errorResponse(HttpStatus status, String message) {
        BaseResponseDto<T> resp = new BaseResponseDto<>();
        resp.setStatus(status.value());
        resp.setMessage(message);
        resp.setTimestamp(new java.util.Date());
        resp.setData(null);
        return resp;
    }
}
