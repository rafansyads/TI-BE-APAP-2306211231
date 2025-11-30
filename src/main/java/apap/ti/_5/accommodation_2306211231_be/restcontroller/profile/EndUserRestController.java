package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerGetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerSetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.EndUserUpdateRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class EndUserRestController {

    private final EndUserRestService endUserRestService;

    @GetMapping("/users")
    public ResponseEntity<BaseResponseDto<List<EndUserResponseDTO>>> getAllEndUsers(
            @RequestParam(required = false) String role) {
        try {
            List<EndUserResponseDTO> users = endUserRestService.getAllEndUsersByRole(role);
            return ResponseUtil.success(users, "OK", HttpStatus.OK).toBuilder().build();
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            return ResponseUtil.error("Forbidden: only SUPERADMIN can access", HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<BaseResponseDto<List<CustomerResponseDTO>>> getAllCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email) {
        try {
            List<CustomerResponseDTO> customers = endUserRestService.getAllCustomersFiltered(name, email);
            return ResponseUtil.success(customers, "OK", HttpStatus.OK).toBuilder().build();
        } catch (AccessDeniedException ex) {
            return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch customers", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{identifier}")
    public ResponseEntity<BaseResponseDto<EndUserResponseDTO>> getEndUserByIdentifier(@PathVariable String identifier) {
        try {
            EndUserResponseDTO result = endUserRestService.getEndUserDtoByIdentifier(identifier);
            if (result == null)
                return ResponseUtil.error("User not found", HttpStatus.NOT_FOUND);
            return ResponseUtil.success(result, "OK", HttpStatus.OK).toBuilder().build();
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch user detail", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{identifier}")
    public ResponseEntity<BaseResponseDto<EndUserResponseDTO>> updateEndUser(
            @PathVariable String identifier,
            @RequestBody EndUserUpdateRequestDTO dto) {
        try {
            EndUserResponseDTO updated = endUserRestService.updateEndUser(identifier, dto);
            return ResponseUtil.success(updated, "Updated", HttpStatus.OK).toBuilder().build();
        } catch (AccessDeniedException ex) {
            return ResponseUtil.error("You are not authorized to update this user.", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to update user", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/saldo/{identifier}")
    public ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> getCustomerSaldo(
            @PathVariable String identifier,
            @RequestBody CustomerGetSaldoRequestDTO dto) {
        try {
            CustomerSaldoResponseDTO saldoDto = endUserRestService.getCustomerSaldo(identifier, dto);
            return ResponseUtil.success(saldoDto, "OK", HttpStatus.OK).toBuilder().build();
        } catch (AccessDeniedException ex) {
            return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch customer saldo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * New convenience GET endpoint that accepts identifier, userId and username as query params.
     * This mirrors the existing logic but allows callers to use GET with query params instead
     * of supplying a request body (useful for browsers that avoid GET bodies).
     */
    @GetMapping("/saldo/identity")
    public ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> getCustomerSaldoByParams(
            @RequestParam(required = false) String identifier,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String name) {
        try {
            // Resolve username when not provided: try to lookup by userId or by name
            String resolvedUsername = username == null ? "" : username;
            if ((resolvedUsername == null || resolvedUsername.isBlank()) && (userId != null && !userId.isBlank())) {
                EndUser eu = endUserRestService.findEndUserByIdentifier(userId);
                if (eu != null && eu.getUsername() != null) resolvedUsername = eu.getUsername();
            }
            if ((resolvedUsername == null || resolvedUsername.isBlank()) && (name != null && !name.isBlank())) {
                // try to resolve by name/identifier
                EndUser eu2 = endUserRestService.findEndUserByIdentifier(name);
                if (eu2 != null && eu2.getUsername() != null) resolvedUsername = eu2.getUsername();
            }

            CustomerGetSaldoRequestDTO dto = CustomerGetSaldoRequestDTO.builder()
                    .userId(userId == null ? "" : userId)
                    .username(resolvedUsername == null ? "" : resolvedUsername)
                    .build();
            // Prefer explicit identifier param; fall back to empty string if not provided
            String id = identifier == null ? "" : identifier;
            CustomerSaldoResponseDTO saldoDto = endUserRestService.getCustomerSaldo(id, dto);
            return ResponseUtil.success(saldoDto, "OK", HttpStatus.OK).toBuilder().build();
        } catch (AccessDeniedException ex) {
            return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to fetch customer saldo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/saldo/{identifier}")
    public ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> setCustomerSaldo(
            @PathVariable String identifier,
            @RequestBody CustomerSetSaldoRequestDTO dto) {
        try {
            CustomerSaldoResponseDTO saldoDto = endUserRestService.setCustomerSaldo(identifier, dto);
            return ResponseUtil.success(saldoDto, "Updated", HttpStatus.OK).toBuilder().build();
        } catch (AccessDeniedException ex) {
            return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
        } catch (IllegalArgumentException ex) {
            return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            return ResponseUtil.error("Failed to update customer saldo", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}