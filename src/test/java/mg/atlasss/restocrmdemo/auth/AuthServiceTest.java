package mg.atlasss.restocrmdemo.auth;

import mg.atlasss.restocrmdemo.auth.dto.LoginRequest;
import mg.atlasss.restocrmdemo.auth.dto.LoginResponse;
import mg.atlasss.restocrmdemo.group.Group;
import mg.atlasss.restocrmdemo.security.JwtProperties;
import mg.atlasss.restocrmdemo.security.JwtService;
import mg.atlasss.restocrmdemo.security.RefreshToken;
import mg.atlasss.restocrmdemo.security.RefreshTokenRepository;
import mg.atlasss.restocrmdemo.user.Role;
import mg.atlasss.restocrmdemo.user.User;
import mg.atlasss.restocrmdemo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequest validRequest;

    @BeforeEach
    void setUp() {
        Group group = Group.builder().id(1L).name("Test Group").build();
        activeUser = User.builder()
                .id(10L)
                .email("user@resto-demo.test")
                .passwordHash("hashed_password_123")
                .fullName("John Doe")
                .role(Role.STAFF)
                .group(group)
                .isActive(true)
                .build();

        validRequest = new LoginRequest("user@resto-demo.test", "plain_password_123");
    }

    @Test
    void shouldLoginSuccessfullyAndReturnTokensWhenCredentialsAreValid() {
        // --- ARRANGE ---
        given(userRepository.findByEmailIgnoreCase(validRequest.email())).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(validRequest.password(), activeUser.getPasswordHash())).willReturn(true);
        given(jwtService.generateAccessToken(activeUser)).willReturn("mock-access-token");
        given(jwtService.generateRefreshToken(activeUser)).willReturn("mock-refresh-token");
        given(jwtProperties.getRefreshTokenTtlDays()).willReturn(7L);
        given(jwtProperties.getAccessTokenTtlMinutes()).willReturn(15L);

        // --- ACT ---
        LoginResponse response = authService.login(validRequest);

        // --- ASSERT ---
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("mock-access-token");
        assertThat(response.refreshToken()).isEqualTo("mock-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresInSeconds()).isEqualTo(900); // 15 minutes in seconds
        assertThat(response.user().id()).isEqualTo(10L);

        // Verify that login time was recorded and refresh token was persisted
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        assertThat(activeUser.getLastLoginAt()).isNotNull();
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenEmailDoesNotExist() {
        // --- ARRANGE ---
        given(userRepository.findByEmailIgnoreCase(validRequest.email())).willReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.login(validRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAccountIsInactive() {
        // --- ARRANGE ---
        activeUser.deactivate();
        given(userRepository.findByEmailIgnoreCase(validRequest.email())).willReturn(Optional.of(activeUser));

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.login(validRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Account inactive");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenPasswordDoesNotMatch() {
        // --- ARRANGE ---
        given(userRepository.findByEmailIgnoreCase(validRequest.email())).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(validRequest.password(), activeUser.getPasswordHash())).willReturn(false);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> authService.login(validRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Wrong password");
    }

    @Test
    void shouldHashRefreshTokenBeforeSavingToDatabase() {
        // --- ARRANGE ---
        given(userRepository.findByEmailIgnoreCase(validRequest.email())).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(validRequest.password(), activeUser.getPasswordHash())).willReturn(true);
        given(jwtService.generateAccessToken(activeUser)).willReturn("access-token");
        given(jwtService.generateRefreshToken(activeUser)).willReturn("raw-secret-refresh-token");
        given(jwtProperties.getRefreshTokenTtlDays()).willReturn(7L);
        given(jwtProperties.getAccessTokenTtlMinutes()).willReturn(15L);

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);

        // --- ACT ---
        authService.login(validRequest);

        // --- ASSERT ---
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        RefreshToken savedToken = tokenCaptor.getValue();

        // Ensure the raw token value is never stored directly in plaintext database records
        assertThat(savedToken.getTokenHash()).isNotEqualTo("raw-secret-refresh-token");
        assertThat(savedToken.getTokenHash()).hasSize(64); // SHA-256 hex string length
    }
}