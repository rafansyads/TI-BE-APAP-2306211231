package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import apap.ti._5.accommodation_2306211231_be.util.ResponseUtil;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
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
    public ResponseEntity<BaseResponseDto<List<EndUserResponseDTO>>> getAllEndUsers(@RequestParam(required = false) String role) {
        try {
          List<EndUserResponseDTO> users = endUserRestService.getAllEndUsersByRole(role);
          return ResponseUtil.success(users, "OK", HttpStatus.OK);
        } catch (org.springframework.security.access.AccessDeniedException ex) {
          return ResponseUtil.error("Forbidden: only SUPERADMIN can access", HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
          return ResponseUtil.error("Failed to fetch users", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/customers")
    public ResponseEntity<BaseResponseDto<List<CustomerResponseDTO>>> getAllCustomers(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String email
    ) {
      try {
        List<apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerResponseDTO> customers = endUserRestService.getAllCustomersFiltered(name, email);
        return ResponseUtil.success(customers, "OK", HttpStatus.OK);
      } catch (AccessDeniedException ex) {
        return ResponseUtil.error("You are not authorized to access this resource.", HttpStatus.FORBIDDEN);
      } catch (Exception ex) {
        return ResponseUtil.error("Failed to fetch customers", HttpStatus.INTERNAL_SERVER_ERROR);
      }
    }
}
