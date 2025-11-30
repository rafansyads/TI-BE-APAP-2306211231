package apap.ti._5.accommodation_2306211231_be.restcontroller;

import apap.ti._5.accommodation_2306211231_be.restdto.request.auth.TokenRefreshRequestDTO;
import apap.ti._5.accommodation_2306211231_be.restservice.AuthRestService;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.RefreshTokenService;
import apap.ti._5.accommodation_2306211231_be.security.service.JwtTokenBlacklist;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import apap.ti._5.accommodation_2306211231_be.security.jwt.JwtUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthRestController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class AuthRestControllerAdditionalTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private AuthRestService authRestService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private RefreshTokenService refreshTokenService;


    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtTokenBlacklist jwtTokenBlacklist;

    @MockBean
    private EntityManagerFactory entityManagerFactory;

    @MockBean
    private org.springframework.web.client.RestTemplate restTemplate;

    @Test
    void register_missingData_returnsBadRequest() throws Exception {
        // empty object -> data == null
        mvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing data object"));
    }

    @Test
    void refresh_missingRefreshHeader_returnsBadRequest() throws Exception {
        TokenRefreshRequestDTO body = new TokenRefreshRequestDTO();
        body.setUsername("user1");
        body.setEmail("u@example.com");

        var wrapper = new apap.ti._5.accommodation_2306211231_be.restdto.BaseRequestDto<TokenRefreshRequestDTO>();
        wrapper.setData(body);

        mvc.perform(post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing Refresh-Token header"));
    }
}
