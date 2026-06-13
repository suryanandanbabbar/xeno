package ai.xenopilot.security;

import ai.xenopilot.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(appProperties.jwt().expirationMinutes() * 60);
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey())
                .compact();
    }

    public String extractUsername(String token) {
        return claims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return userDetails.getUsername().equals(extractUsername(token)) && claims(token).getExpiration().after(new Date());
    }

    private Claims claims(String token) {
        return Jwts.parser().verifyWith((SecretKey) signingKey()).build().parseSignedClaims(token).getPayload();
    }

    private Key signingKey() {
        String secret = appProperties.jwt().secret();
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (Exception ignored) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }   
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
