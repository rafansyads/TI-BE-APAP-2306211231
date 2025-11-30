package apap.ti._5.accommodation_2306211231_be.restcontroller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto;
import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.RegisterRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
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
class AuthRestControllerMvcTests {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthRestService authRestService;

    @MockBean
    org.springframework.security.authentication.AuthenticationManager authenticationManager;

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
    void register_missingData_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void register_superadmin_forbidden() throws Exception {
        String body = "{\"data\":{\"username\":\"u\",\"email\":\"e@x.com\",\"role\":\"SUPERADMIN\"}}";
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void register_usernameTaken_returnsConflict() throws Exception {
        String body = "{\"data\":{\"username\":\"u\",\"email\":\"e@x.com\",\"role\":\"CUSTOMER\"}}";
        when(authRestService.existsUsername(eq("u"))).thenReturn(true);
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void refresh_missingRefreshHeader_returnsBadRequest() throws Exception {
        String body = "{\"data\":{\"username\":\"u\",\"email\":\"e@x.com\"}}";
        mockMvc.perform(post("/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void forward_missingBodyOrTargetUrl_returnsBadRequest() throws Exception {
        // missing data
        mockMvc.perform(post("/auth/forward").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        // data present but missing targetUrl
        String body = "{\"data\":{\"targetUrl\":\"\"}}";
        mockMvc.perform(post("/auth/forward").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
