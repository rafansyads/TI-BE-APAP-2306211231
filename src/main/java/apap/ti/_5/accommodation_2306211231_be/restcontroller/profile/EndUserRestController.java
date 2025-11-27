package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.EndUserUpdateRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

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

    // @GetMapping("/saldo/{identifier}")
    // public ResponseEntity<BaseResponseDto<Integer>> getCustomerSaldo(@PathVariable String identifier) {
    //     try {
    //         Integer saldo = endUserRestService.getCustomerSaldoByIdentifier(identifier);
    //         return ResponseUtil.success(saldo, "OK", HttpStatus.OK).toBuilder().build();
    //     } catch (AccessDeniedException ex) {
    //         return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
    //     } catch (IllegalArgumentException ex) {
    //         return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    //     } catch (Exception ex) {
    //         return ResponseUtil.error("Failed to fetch customer saldo", HttpStatus.INTERNAL_SERVER_ERROR);
    //     }
    // }

    // @PutMapping("/saldo/{identifier}")
    // public ResponseEntity<BaseResponseDto<Integer>> updateCustomerSaldo(
    //         @PathVariable String identifier,
    //         @RequestParam Integer amount) {
    //     try {
    //         Integer updatedSaldo = endUserRestService.updateCustomerSaldoByIdentifier(identifier, amount);
    //         return ResponseUtil.success(updatedSaldo, "Updated", HttpStatus.OK).toBuilder().build();
    //     } catch (AccessDeniedException ex) {
    //         return ResponseUtil.error("You are not authorized to update this resource.", HttpStatus.FORBIDDEN);
    //     } catch (IllegalArgumentException ex) {
    //         return ResponseUtil.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    //     } catch (Exception ex) {
    //         return ResponseUtil.error("Failed to update customer saldo", HttpStatus.INTERNAL_SERVER_ERROR);
    //     }
    // }
}
