package apap.ti._5.accommodation_2306211231_be.restcontroller.profile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import apap.ti._5.accommodation_2306211231_be.models.profile.Customer;
import apap.ti._5.accommodation_2306211231_be.restdto.response.profile.CustomerSaldoResponseDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.profile.EndUserRestService;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EndUserRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class EndUserRestControllerMvcTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EndUserRestService endUserRestService;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @Test
    void getCustomerSaldo_withRequestBody_success() throws Exception {
        when(endUserRestService.getCustomerSaldo(eq("id"), any())).thenReturn(new CustomerSaldoResponseDTO());

        mockMvc.perform(get("/profile/saldo/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\",\"username\":\"user1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getCustomerSaldo_withRequestBody_badRequest() throws Exception {
        when(endUserRestService.getCustomerSaldo(eq("id"), any())).thenThrow(new IllegalArgumentException("bad"));

        mockMvc.perform(get("/profile/saldo/id")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"u1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("bad"));
    }

    @Test
    void getCustomerSaldoByParams_resolvesUsernameAndCallsService() throws Exception {
        Customer eu = new Customer(); eu.setUsername("user1");
        when(endUserRestService.findEndUserByIdentifier("u1")).thenReturn(eu);
        when(endUserRestService.getCustomerSaldo(eq(""), any())).thenReturn(new CustomerSaldoResponseDTO());

        mockMvc.perform(get("/profile/saldo/identity").param("userId", "u1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));
    }
}
