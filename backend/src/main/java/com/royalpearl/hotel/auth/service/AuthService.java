package com.royalpearl.hotel.auth.service;

import com.royalpearl.hotel.auth.dto.AuthResponse;
import com.royalpearl.hotel.auth.dto.LoginRequest;
import com.royalpearl.hotel.auth.dto.RefreshRequest;
import com.royalpearl.hotel.auth.dto.RegisterRequest;
import com.royalpearl.hotel.auth.entity.RefreshToken;
import com.royalpearl.hotel.auth.repository.RefreshTokenRepository;
import com.royalpearl.hotel.exception.BadRequestException;
import com.royalpearl.hotel.exception.ConflictException;
import com.royalpearl.hotel.security.JwtService;
import com.royalpearl.hotel.user.entity.AppRole;
import com.royalpearl.hotel.user.entity.User;
import com.royalpearl.hotel.user.entity.UserRole;
import com.royalpearl.hotel.user.repository.UserRepository;
import com.royalpearl.hotel.user.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository         userRepository;
    private final UserRoleRepository     userRoleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder        passwordEncoder;
    private final JwtService             jwtService;

    // ---------------------------------------------------------------
    // Register
    // ---------------------------------------------------------------

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail().toLowerCase())) {
            throw new ConflictException("Email already registered");
        }

        User user = userRepository.save(User.builder()
                .email(req.getEmail().toLowerCase())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .provider("email")
                .emailVerified(true)
                .build());

        userRoleRepository.save(UserRole.builder()
                .user(user)
                .role(AppRole.user)
                .build());

        return buildAuthResponse(user, List.of(AppRole.user.name()));
    }

    // ---------------------------------------------------------------
    // Login
    // ---------------------------------------------------------------

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null ||
                !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getRole().name())
                .toList();

        return buildAuthResponse(user, roles);
    }

    // ---------------------------------------------------------------
    // Refresh
    // ---------------------------------------------------------------

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        String hash = jwtService.hashToken(req.getRefreshToken());

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Refresh token expired or revoked");
        }

        // Rotate: revoke old, issue new
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        User user = stored.getUser();
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getRole().name())
                .toList();

        return buildAuthResponse(user, roles);
    }

    // ---------------------------------------------------------------
    // Logout
    // ---------------------------------------------------------------

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("User {} logged out – all refresh tokens revoked", userId);
    }

    // ---------------------------------------------------------------
    // Scheduled cleanup of expired tokens (runs daily at 03:00)
    // ---------------------------------------------------------------

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(OffsetDateTime.now());
        log.info("Purged expired refresh tokens");
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private AuthResponse buildAuthResponse(User user, List<String> roles) {
        String accessToken  = jwtService.generateAccessToken(user.getId(), roles);
        String rawRefresh   = jwtService.generateRawRefreshToken();
        String refreshHash  = jwtService.hashToken(rawRefresh);

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(refreshHash)
                .expiresAt(OffsetDateTime.now().plusSeconds(jwtService.getRefreshTokenTtlSeconds()))
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefresh)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenTtlSeconds())
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .avatarUrl(user.getAvatarUrl())
                        .roles(roles)
                        .build())
                .build();
    }
}
