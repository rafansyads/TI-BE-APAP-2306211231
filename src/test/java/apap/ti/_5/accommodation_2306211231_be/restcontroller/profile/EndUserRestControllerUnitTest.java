package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import apap.ti._5.accommodation_2306211231_be.models.profile.EndUser;
import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerGetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.EndUserResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EndUserRestControllerUnitTest {

    @Test
    void getAllEndUsers_success_returnsOk() {
        var svc = mock(EndUserRestService.class);
        when(svc.getAllEndUsersByRole(null)).thenReturn(List.of(new EndUserResponseDTO()));
        var controller = new EndUserRestController(svc);
        ResponseEntity<?> resp = controller.getAllEndUsers(null);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void getAllCustomers_forbidden_onAccessDenied() {
        var svc = mock(EndUserRestService.class);
        when(svc.getAllCustomersFiltered(any(), any())).thenThrow(new org.springframework.security.access.AccessDeniedException("no"));
        var controller = new EndUserRestController(svc);
        ResponseEntity<?> resp = controller.getAllCustomers(null, null);
        assertEquals(403, resp.getStatusCodeValue());
    }

    @Test
    void getEndUserByIdentifier_notFound_returns404() {
        var svc = mock(EndUserRestService.class);
        when(svc.getEndUserDtoByIdentifier("nope")).thenReturn(null);
        var controller = new EndUserRestController(svc);
        var resp = controller.getEndUserByIdentifier("nope");
        assertEquals(404, resp.getStatusCodeValue());
    }

    @Test
    void updateEndUser_badRequest_onIllegalArgument() {
        var svc = mock(EndUserRestService.class);
        when(svc.updateEndUser(any(), any())).thenThrow(new IllegalArgumentException("bad"));
        var controller = new EndUserRestController(svc);
        var resp = controller.updateEndUser("id", null);
        assertEquals(400, resp.getStatusCodeValue());
    }

    @Test
    void getCustomerSaldoByParams_resolvesUsernameAndCallsService() {
        var svc = mock(EndUserRestService.class);
        apap.ti._5.accommodation_2306211231_be.models.profile.Customer eu = new apap.ti._5.accommodation_2306211231_be.models.profile.Customer(); eu.setUsername("user1");
        when(svc.findEndUserByIdentifier("u1")).thenReturn(eu);
        when(svc.getCustomerSaldo(any(), any())).thenReturn(new CustomerSaldoResponseDTO());
        var controller = new EndUserRestController(svc);
        var resp = controller.getCustomerSaldoByParams(null, "u1", null, null);
        assertEquals(200, resp.getStatusCodeValue());
    }

    @Test
    void setCustomerSaldo_success_returnsUpdated() {
        var svc = mock(EndUserRestService.class);
        when(svc.setCustomerSaldo(eq("id"), any())).thenReturn(new CustomerSaldoResponseDTO());
        var controller = new EndUserRestController(svc);
        var resp = controller.setCustomerSaldo("id", null);
        assertEquals(200, resp.getStatusCodeValue());
    }
}
