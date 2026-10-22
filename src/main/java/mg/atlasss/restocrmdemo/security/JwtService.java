package mg.atlasss.restocrmdemo.security;

import mg.atlasss.restocrmdemo.user.Role;
import mg.atlasss.restocrmdemo.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_GROUP_ID = "groupId";
    private static final String CLAIM_RESTAURANT_ID = "restaurantId";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, Duration.ofMinutes(properties.getAccessTokenTtlMinutes()));
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, Duration.ofDays(properties.getRefreshTokenTtlDays()));
    }

    private String buildToken(User user, String type, Duration ttl) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_GROUP_ID, user.getGroup().getId())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key);
        if (user.getRestaurant() != null) {
            builder.claim(CLAIM_RESTAURANT_ID, user.getRestaurant().getId());
        }
        return builder.compact();
    }

    public AuthenticatedPrincipal parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        requireType(claims, TYPE_ACCESS);
        return toPrincipal(claims);
    }

    public AuthenticatedPrincipal parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        requireType(claims, TYPE_REFRESH);
        return toPrincipal(claims);
    }

    private void requireType(Claims claims, String expectedType) {
        String actual = claims.get(CLAIM_TYPE, String.class);
        if (!expectedType.equals(actual)) {
            throw new JwtException("Unexpected token type: expected " + expectedType + ", got " + actual);
        }
    }

    private AuthenticatedPrincipal toPrincipal(Claims claims) {
        Long userId = Long.valueOf(claims.getSubject());
        String email = claims.get(CLAIM_EMAIL, String.class);
        Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));
        Long groupId = claims.get(CLAIM_GROUP_ID, Long.class);
        Long restaurantId = claims.get(CLAIM_RESTAURANT_ID, Number.class) == null
                ? null
                : claims.get(CLAIM_RESTAURANT_ID, Number.class).longValue();
        return new AuthenticatedPrincipal(userId, email, role, groupId, restaurantId);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
