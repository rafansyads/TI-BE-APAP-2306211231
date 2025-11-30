package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import apap.ti._5.accommodation_2306211231_be.restdto.request.profile.CustomerGetSaldoRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restcontroller.profile.EndUserRestController;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;

@WebMvcTest(controllers = EndUserRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EndUserRestControllerMvcAdditionalTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private EndUserRestService endUserRestService;

    // common mocks that the WebMvc slice may require in this project
    @MockBean
    private apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;
    @MockBean
    private apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService authRestService;

    @Test
    void getSaldoIdentity_resolvesUsernameAndReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        var dto = new CustomerSaldoResponseDTO();
        dto.setUsername("alice");
        dto.setUserId(idStr);
        dto.setSaldo(200L);

        when(endUserRestService.findEndUserByIdentifier(anyString())).thenReturn(new apap.ti._5.accommodation_2306211231_be.models.profile.EndUser(){
            { setUsername("alice"); setId(id); }
        });
        when(endUserRestService.getCustomerSaldo(anyString(), any(CustomerGetSaldoRequestDTO.class))).thenReturn(dto);

        mvc.perform(get("/profile/saldo/identity").param("userId", idStr))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.saldo").value(200));
    }

    @Test
    void getCustomerSaldo_withBody_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        String idStr = id.toString();

        var req = CustomerGetSaldoRequestDTO.builder().username("bob").userId(idStr).build();

        var dto = new CustomerSaldoResponseDTO();
        dto.setUsername("bob");
        dto.setUserId(idStr);
        dto.setSaldo(50L);

        when(endUserRestService.getCustomerSaldo(anyString(), any(CustomerGetSaldoRequestDTO.class))).thenReturn(dto);

        mvc.perform(get("/profile/saldo/{identifier}", idStr)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("bob"))
                .andExpect(jsonPath("$.data.saldo").value(50));
    }
}
