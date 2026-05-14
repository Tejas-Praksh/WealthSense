package com.wealthsense.user.service;

import com.wealthsense.common.exception.DuplicateResourceException;
import com.wealthsense.common.exception.UnauthorizedException;
import com.wealthsense.common.util.CorrelationIdUtil;
import com.wealthsense.security.aop.Auditable;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.user.domain.RefreshToken;
import com.wealthsense.user.domain.User;
import com.wealthsense.user.dto.AuthResponse;
import com.wealthsense.user.dto.LoginRequest;
import com.wealthsense.user.dto.RegisterRequest;
import com.wealthsense.user.mapper.UserMapper;
import com.wealthsense.user.repository.UserRepository;
import com.wealthsense.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.wealthsense.common.constants.SecurityConstants.MAX_LOGIN_ATTEMPTS;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final AESEncryptionService aesEncryptionService;

    @Transactional
    @Auditable(action = "REGISTER", resource = "USER")
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(aesEncryptionService.encrypt(request.getPhone()));
        }
        userRepository.save(user);

        log.info("User registered successfully: {}", request.getEmail());
        return "Registration successful. Please verify your email.";
    }

    @Transactional
    @Auditable(action = "LOGIN", resource = "AUTH")
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.isAccountLocked()) {
            log.warn("Login attempt on locked account: {}", request.getEmail());
            throw new UnauthorizedException("Account is locked due to too many failed attempts");
        }

        if (!user.isEmailVerified()) {
            log.warn("Login attempt with unverified email: {}", request.getEmail());
            throw new UnauthorizedException("Email not verified. Please verify your email first");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Reset failed attempts on successful login
        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = tokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();

        String newRefreshToken = tokenService.rotateRefreshToken(refreshToken);
        String accessToken = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(),
                user.getRole().name(),
                CorrelationIdUtil.getCurrentCorrelationId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .userRole(user.getRole().name())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        tokenService.revokeRefreshToken(refreshToken);
        log.info("User logged out successfully");
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            log.warn("Account locked after {} failed attempts: {}", attempts, user.getEmail());
        }

        userRepository.save(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateToken(
                user.getId(), user.getEmail(),
                user.getRole().name(),
                CorrelationIdUtil.getCurrentCorrelationId());

        String refreshToken = tokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getJwtExpiration())
                .userRole(user.getRole().name())
                .build();
    }
}
