package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(controllers = AuthRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
class AuthRestControllerLoginTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService authRestService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils jwtUtils;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService jwtTokenService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService refreshTokenService;

    @MockBean
    apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist jwtTokenBlacklist;

    @MockBean
    org.springframework.web.client.RestTemplate restTemplate;

    @MockBean
    UserDetailsService userDetailsService;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @Test
    void login_missingData_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
