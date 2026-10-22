package mg.atlasss.restocrmdemo.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HexFormat;

import mg.atlasss.restocrmdemo.auth.dto.LoginRequest;
import mg.atlasss.restocrmdemo.auth.dto.LoginResponse;
import mg.atlasss.restocrmdemo.auth.dto.UserSummary;
import mg.atlasss.restocrmdemo.security.JwtProperties;
import mg.atlasss.restocrmdemo.security.JwtService;
import mg.atlasss.restocrmdemo.security.RefreshToken;
import mg.atlasss.restocrmdemo.security.RefreshTokenRepository;
import mg.atlasss.restocrmdemo.user.User;
import mg.atlasss.restocrmdemo.user.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, JwtProperties jwtProperties, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!user.getIsActive()) {
            throw new BadCredentialsException("Account inactive");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Wrong password");
        }
        user.recordLogin(OffsetDateTime.now());
        return issueTokens(user);
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        var expiresAt = OffsetDateTime.now().plus(Duration.ofDays(jwtProperties.getRefreshTokenTtlDays()));
        refreshTokenRepository.save(new RefreshToken(user, sha256(refreshTokenValue), expiresAt));

        long expiresInSeconds = Duration.ofMinutes(jwtProperties.getAccessTokenTtlMinutes()).toSeconds();
        return new LoginResponse(accessToken, refreshTokenValue, "Bearer", expiresInSeconds, UserSummary.from(user));
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
