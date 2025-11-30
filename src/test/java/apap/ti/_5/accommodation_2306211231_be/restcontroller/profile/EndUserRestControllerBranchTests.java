package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.BaseResponseDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.*;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.*;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EndUserRestControllerBranchTests {

    @Mock
    private EndUserRestService endUserRestService;

    private EndUserRestController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new EndUserRestController(endUserRestService);
    }

    @Test
    void getAllEndUsers_success_returnsUsers() {
        List<EndUserResponseDTO> users = List.of(new EndUserResponseDTO());
        when(endUserRestService.getAllEndUsersByRole(null)).thenReturn(users);

        ResponseEntity<BaseResponseDto<List<EndUserResponseDTO>>> response = controller.getAllEndUsers(null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().getData().size());
    }

    @Test
    void getAllEndUsers_accessDenied_returns403() {
        when(endUserRestService.getAllEndUsersByRole(anyString()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<List<EndUserResponseDTO>>> response = controller.getAllEndUsers("CUSTOMER");

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getAllEndUsers_exception_returns500() {
        when(endUserRestService.getAllEndUsersByRole(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<List<EndUserResponseDTO>>> response = controller.getAllEndUsers(null);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void getAllCustomers_success_returnsCustomers() {
        List<CustomerResponseDTO> customers = List.of(new CustomerResponseDTO());
        when(endUserRestService.getAllCustomersFiltered(null, null)).thenReturn(customers);

        ResponseEntity<BaseResponseDto<List<CustomerResponseDTO>>> response = 
            controller.getAllCustomers(null, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getAllCustomers_accessDenied_returns403() {
        when(endUserRestService.getAllCustomersFiltered(anyString(), any()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<List<CustomerResponseDTO>>> response = 
            controller.getAllCustomers("name", null);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getAllCustomers_exception_returns500() {
        when(endUserRestService.getAllCustomersFiltered(any(), any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<List<CustomerResponseDTO>>> response = 
            controller.getAllCustomers(null, null);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void getEndUserByIdentifier_success_returnsUser() {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        when(endUserRestService.getEndUserDtoByIdentifier("user1")).thenReturn(dto);

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.getEndUserByIdentifier("user1");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getEndUserByIdentifier_notFound_returns404() {
        when(endUserRestService.getEndUserDtoByIdentifier("unknown")).thenReturn(null);

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.getEndUserByIdentifier("unknown");

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void getEndUserByIdentifier_exception_returns500() {
        when(endUserRestService.getEndUserDtoByIdentifier(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.getEndUserByIdentifier("user1");

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void updateEndUser_success_returnsUpdated() {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        EndUserUpdateRequestDTO updateDto = new EndUserUpdateRequestDTO();
        when(endUserRestService.updateEndUser("user1", updateDto)).thenReturn(dto);

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.updateEndUser("user1", updateDto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateEndUser_accessDenied_returns403() {
        EndUserUpdateRequestDTO updateDto = new EndUserUpdateRequestDTO();
        when(endUserRestService.updateEndUser(anyString(), any()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.updateEndUser("user1", updateDto);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void updateEndUser_illegalArgument_returns400() {
        EndUserUpdateRequestDTO updateDto = new EndUserUpdateRequestDTO();
        when(endUserRestService.updateEndUser(anyString(), any()))
            .thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.updateEndUser("user1", updateDto);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void updateEndUser_exception_returns500() {
        EndUserUpdateRequestDTO updateDto = new EndUserUpdateRequestDTO();
        when(endUserRestService.updateEndUser(anyString(), any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<EndUserResponseDTO>> response = 
            controller.updateEndUser("user1", updateDto);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldo_success_returnsSaldo() {
        CustomerSaldoResponseDTO dto = new CustomerSaldoResponseDTO();
        CustomerGetSaldoRequestDTO reqDto = new CustomerGetSaldoRequestDTO();
        when(endUserRestService.getCustomerSaldo("id1", reqDto)).thenReturn(dto);

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldo("id1", reqDto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldo_accessDenied_returns403() {
        CustomerGetSaldoRequestDTO reqDto = new CustomerGetSaldoRequestDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldo("id1", reqDto);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldo_illegalArgument_returns400() {
        CustomerGetSaldoRequestDTO reqDto = new CustomerGetSaldoRequestDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any()))
            .thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldo("id1", reqDto);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldo_exception_returns500() {
        CustomerGetSaldoRequestDTO reqDto = new CustomerGetSaldoRequestDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldo("id1", reqDto);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldoByParams_success_returnsSaldo() {
        CustomerSaldoResponseDTO dto = new CustomerSaldoResponseDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any())).thenReturn(dto);

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldoByParams("id1", null, "user", null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldoByParams_resolvesUsernameFromUserId() {
        Customer customer = new Customer();
        customer.setUsername("resolved");
        when(endUserRestService.findEndUserByIdentifier("userId123")).thenReturn(customer);
        
        CustomerSaldoResponseDTO dto = new CustomerSaldoResponseDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any())).thenReturn(dto);

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldoByParams(null, "userId123", null, null);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldoByParams_resolvesUsernameFromName() {
        Customer customer = new Customer();
        customer.setUsername("resolvedFromName");
        when(endUserRestService.findEndUserByIdentifier("John")).thenReturn(customer);
        
        CustomerSaldoResponseDTO dto = new CustomerSaldoResponseDTO();
        when(endUserRestService.getCustomerSaldo(anyString(), any())).thenReturn(dto);

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldoByParams(null, null, null, "John");

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldoByParams_accessDenied_returns403() {
        when(endUserRestService.getCustomerSaldo(anyString(), any()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldoByParams(null, null, "user", null);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void getCustomerSaldoByParams_exception_returns500() {
        when(endUserRestService.getCustomerSaldo(anyString(), any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.getCustomerSaldoByParams(null, null, "user", null);

        assertEquals(500, response.getStatusCode().value());
    }

    @Test
    void setCustomerSaldo_success_returnsSaldo() {
        CustomerSaldoResponseDTO dto = new CustomerSaldoResponseDTO();
        CustomerSetSaldoRequestDTO reqDto = new CustomerSetSaldoRequestDTO();
        when(endUserRestService.setCustomerSaldo("id1", reqDto)).thenReturn(dto);

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.setCustomerSaldo("id1", reqDto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void setCustomerSaldo_accessDenied_returns403() {
        CustomerSetSaldoRequestDTO reqDto = new CustomerSetSaldoRequestDTO();
        when(endUserRestService.setCustomerSaldo(anyString(), any()))
            .thenThrow(new AccessDeniedException("Forbidden"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.setCustomerSaldo("id1", reqDto);

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void setCustomerSaldo_illegalArgument_returns400() {
        CustomerSetSaldoRequestDTO reqDto = new CustomerSetSaldoRequestDTO();
        when(endUserRestService.setCustomerSaldo(anyString(), any()))
            .thenThrow(new IllegalArgumentException("Invalid"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.setCustomerSaldo("id1", reqDto);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void setCustomerSaldo_exception_returns500() {
        CustomerSetSaldoRequestDTO reqDto = new CustomerSetSaldoRequestDTO();
        when(endUserRestService.setCustomerSaldo(anyString(), any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<BaseResponseDto<CustomerSaldoResponseDTO>> response = 
            controller.setCustomerSaldo("id1", reqDto);

        assertEquals(500, response.getStatusCode().value());
    }
}
