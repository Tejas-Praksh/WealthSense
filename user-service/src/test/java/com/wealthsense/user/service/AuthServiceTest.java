package com.wealthsense.user.service;

import com.wealthsense.common.exception.DuplicateResourceException;
import com.wealthsense.common.exception.UnauthorizedException;
import com.wealthsense.common.enums.UserRole;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.user.domain.RefreshToken;
import com.wealthsense.user.domain.User;
import com.wealthsense.user.dto.AuthResponse;
import com.wealthsense.user.dto.LoginRequest;
import com.wealthsense.user.dto.RegisterRequest;
import com.wealthsense.user.mapper.UserMapper;
import com.wealthsense.user.repository.RefreshTokenRepository;
import com.wealthsense.user.repository.UserRepository;
import com.wealthsense.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenService tokenService;
    @Mock
    private AESEncryptionService aesEncryptionService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@wealthsense.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.USER)
                .enabled(true)
                .emailVerified(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .build();

        registerRequest = RegisterRequest.builder()
                .email("new@wealthsense.com")
                .password("Password1!")
                .firstName("New")
                .lastName("User")
                .phone("+911234567890")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@wealthsense.com")
                .password("Password1!")
                .build();
    }

    @Test
    void register_validRequest_createsUser() {
        User mappedUser = User.builder()
                .email(registerRequest.getEmail())
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .build();

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(mappedUser);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPwd");
        when(aesEncryptionService.encrypt(registerRequest.getPhone())).thenReturn("enc-phone");
        when(userRepository.save(any(User.class))).thenReturn(mappedUser);

        String result = authService.register(registerRequest);

        assertNotNull(result);
        verify(userRepository).existsByEmail(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(aesEncryptionService).encrypt(registerRequest.getPhone());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsTokens() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateToken(any(), any(), any(), any()))
                .thenReturn("access-token");
        when(jwtTokenProvider.getJwtExpiration()).thenReturn(900000L);
        when(tokenService.createRefreshToken(any(User.class)))
                .thenReturn("refresh-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("USER", response.getUserRole());
    }

    @Test
    void login_wrongPassword_incrementsFailedAttempts() {
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
        verify(userRepository).save(testUser);
        assertEquals(1, testUser.getFailedLoginAttempts());
    }

    @Test
    void login_after5Failures_locksAccount() {
        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
        assertTrue(testUser.isAccountLocked());
        assertEquals(5, testUser.getFailedLoginAttempts());
    }

    @Test
    void login_lockedAccount_throwsException() {
        testUser.setAccountLocked(true);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void login_unverifiedEmail_throwsException() {
        testUser.setEmailVerified(false);
        when(userRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(testUser));

        assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
    }

    @Test
    void refreshToken_validToken_returnsNewTokens() {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .token("valid-refresh-token")
                .expiryDate(Instant.now().plusSeconds(86400))
                .revoked(false)
                .build();

        when(tokenService.validateRefreshToken("valid-refresh-token"))
                .thenReturn(refreshToken);
        when(jwtTokenProvider.generateToken(any(), any(), any(), any()))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.getJwtExpiration()).thenReturn(900000L);
        when(tokenService.rotateRefreshToken(refreshToken))
                .thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken("valid-refresh-token");

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
    }

    @Test
    void refreshToken_expiredToken_throwsException() {
        when(tokenService.validateRefreshToken("expired-token"))
                .thenThrow(new UnauthorizedException("Refresh token expired"));

        assertThrows(UnauthorizedException.class,
                () -> authService.refreshToken("expired-token"));
    }

    @Test
    void logout_validToken_revokesToken() {
        doNothing().when(tokenService).revokeRefreshToken("valid-token");

        assertDoesNotThrow(() -> authService.logout("valid-token"));
        verify(tokenService).revokeRefreshToken("valid-token");
    }
}
