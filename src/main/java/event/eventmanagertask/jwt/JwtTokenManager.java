package event.eventmanagertask.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenManager {

    private final long expirationTime;
    private final SecretKey secretKey;

    public JwtTokenManager(
            @Value("${jwt.lifetime}") long expirationTime,
            @Value("${jwt.secret-key}") String keyString) {
        this.expirationTime = expirationTime;
        this.secretKey = Keys.hmacShaKeyFor(keyString.getBytes());
    }

    public String generateToken(String login) {
        return Jwts
                .builder()
                .subject(login)
                .signWith(secretKey)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .compact();
    }

    public String getLoginFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String jwtToken) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parse(jwtToken);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public String getRoleFromToken(String jwtToken) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken)
                .getPayload()
                .get("role", String.class);
    }
}
