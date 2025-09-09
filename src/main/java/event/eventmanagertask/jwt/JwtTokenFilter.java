package event.eventmanagertask.jwt;

import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    private final JwtTokenManager jwtTokenManager;

    public JwtTokenFilter(JwtTokenManager jwtTokenManager) {
        this.jwtTokenManager = jwtTokenManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring("Bearer ".length());
        String loginFromToken;

        try {
            if (!jwtTokenManager.isTokenValid(token)) {
                logger.warn("Invalid JWT token");
                filterChain.doFilter(request, response);
                return;
            }
            String login = jwtTokenManager.getLoginFromToken(token);
            Long userId = jwtTokenManager.getUserIdFromToken(token);
            Role role = jwtTokenManager.getRoleFromToken(token);

            logger.debug("Extracted from token - login: {}, userId: {}, role: {}", login, userId, role);

            if (login == null || userId == null || role == null) {
                logger.warn("Missing required claims in token");
                filterChain.doFilter(request, response);
                return;
            }

            // Создаем User объект из данных токена
            User user = User.fromTokenData(userId, login, role);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority(role.name()))
                    );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.debug("Authentication set for user: {}", user);

        } catch (Exception e) {
            logger.error("Error processing JWT token: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
