package event.eventmanagertask.jwt;

import event.eventmanagertask.model.Role;
import event.eventmanagertask.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenManager.class);

    private final long expirationTime;
    private final SecretKey secretKey;

    public JwtTokenManager(
            @Value("${jwt.lifetime}") long expirationTime,
            @Value("${jwt.secret-key}") String keyString) {
        this.expirationTime = expirationTime;
        this.secretKey = Keys.hmacShaKeyFor(keyString.getBytes());
    }

    public String generateToken(User user) {
        if (user == null) {
            logger.error("User with login {} not found", user.login());
            throw new IllegalArgumentException("User not found with login: " + user.login());
        }

        return Jwts
                .builder()
                .subject(user.login())
                .claim("userId", user.id())
                .claim("role", user.role().name())
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String getLoginFromToken(String token) {
        return parseToken(token).getSubject();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public Role getRoleFromToken(String token) {
        String roleString = parseToken(token).get("role", String.class);
        return Role.valueOf(roleString);
    }

    public boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
