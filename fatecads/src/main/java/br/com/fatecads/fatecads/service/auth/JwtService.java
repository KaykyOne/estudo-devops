package br.com.fatecads.fatecads.service.auth;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import br.com.fatecads.fatecads.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtService {

    private static final String RESET_TOKEN_TYPE = "PASSWORD_RESET";

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public String generateLoginToken(String login, String role) {
        return buildToken(
                Map.of("role", role),
                login,
                jwtExpirationMs);
    }

    public String generatePasswordResetToken(Integer userId, String login, Duration duration) {
        return buildToken(
                Map.of(
                        "type", RESET_TOKEN_TYPE,
                        "uid", userId),
                login,
                duration.toMillis());
    }

    public boolean isPasswordResetTokenValid(String token, Usuario usuario) {
        try {
            Claims claims = extractAllClaims(token);

            Object tokenType = claims.get("type");
            Object tokenUserId = claims.get("uid");

            return RESET_TOKEN_TYPE.equals(tokenType)
                    && usuario.getIdUsuario().equals(tokenUserId)
                    && usuario.getLogin().equals(claims.getSubject())
                    && !isExpired(claims);
        } catch (SignatureException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRoleFromToken(String token) {
        Object role = extractAllClaims(token).get("role");
        return role == null ? "ROLE_USER" : role.toString();
    }

    public boolean isLoginTokenValid(String token, String expectedUsername) {
        try {
            Claims claims = extractAllClaims(token);
            return expectedUsername.equals(claims.getSubject())
                    && claims.get("type") == null
                    && !isExpired(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public long getLoginExpirationSeconds() {
        return jwtExpirationMs / 1000;
    }

    private String buildToken(Map<String, Object> claims, String subject, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }
}
