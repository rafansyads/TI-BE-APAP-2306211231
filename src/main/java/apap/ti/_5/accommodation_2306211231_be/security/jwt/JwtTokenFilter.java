package apap.ti._5.accommodation_2306211231_be.security.jwt;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
 
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
 
@Component
public class JwtTokenFilter extends OncePerRequestFilter{
    @Autowired
    private JwtUtils jwtUtils;
 
    @Autowired
    private UserDetailsService userDetailService;
 
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
 
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException{
        try{
            String jwt = parseJwt(request);
            if (jwt != null) {
                boolean valid = jwtUtils.validateJwtToken(jwt);
                if (!valid) {
                    // Token present but invalid (revoked/expired) -> respond 403 Forbidden with JSON payload
                    logger.warn("JWT token present but invalid/revoked");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json;charset=UTF-8");
                    String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    String msg = "Forbidden";
                    String body = String.format("{\"status\":403,\"message\":\"%s\",\"timestamp\":\"%s\",\"data\":null}", msg, timestamp);
                    response.getWriter().write(body);
                    return;
                }

                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                UserDetails userDetails = userDetailService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }catch(Exception e){
            logger.error("Cannot set user authentication: {}", e);
            // Unexpected exception during token processing -> return 401 with JSON body
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            String body = String.format("{\"status\":401,\"message\":\"Unauthorized\",\"timestamp\":\"%s\",\"data\":null}", timestamp);
            response.getWriter().write(body);
            return;
        }
        filterChain.doFilter(request, response);
    }
 
    private String parseJwt(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
 
        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }
}
