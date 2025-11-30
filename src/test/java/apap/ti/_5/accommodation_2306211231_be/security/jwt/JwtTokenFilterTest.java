package apap.ti._5.accommodation_2306211231_be.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

class JwtTokenFilterTest {

    JwtTokenFilter filter;
    JwtUtils jwtUtils;

    @BeforeEach
    void setup() {
        filter = new JwtTokenFilter();
        jwtUtils = mock(JwtUtils.class);
        // inject jwtUtils via reflection
        try {
            var f = JwtTokenFilter.class.getDeclaredField("jwtUtils");
            f.setAccessible(true);
            f.set(filter, jwtUtils);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterEach
    void tearDown() {
        // clear security context
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_internal_setsAuthentication_whenTokenValid() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer sometoken");
        when(jwtUtils.validateJwtToken("sometoken")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("sometoken")).thenReturn("bob");

        // inject a simple UserDetailsService that returns a User
        var uds = mock(org.springframework.security.core.userdetails.UserDetailsService.class);
        when(uds.loadUserByUsername("bob")).thenReturn((UserDetails) User.withUsername("bob").password("x").roles("CUSTOMER").build());
        try {
            var f = JwtTokenFilter.class.getDeclaredField("userDetailService");
            f.setAccessible(true);
            f.set(filter, uds);
        } catch (Exception e) { throw new RuntimeException(e); }

        filter.doFilterInternal(req, resp, chain);

        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals("bob", auth.getName());
        verify(chain, times(1)).doFilter(req, resp);
    }

    @Test
    void doFilter_internal_returns403_whenTokenInvalid() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(jwtUtils.validateJwtToken("badtoken")).thenReturn(false);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        when(resp.getWriter()).thenReturn(pw);

        filter.doFilterInternal(req, resp, chain);

        verify(resp, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);
        pw.flush();
        String body = sw.toString();
        assertTrue(body.contains("Forbidden"));
        verify(chain, never()).doFilter(req, resp);
    }

    @Test
    void doFilter_internal_callsChain_whenNoAuthorizationHeader() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(req, resp, chain);

        // should simply pass through to the chain and not set a forbidden status
        verify(chain, times(1)).doFilter(req, resp);
        verify(resp, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
