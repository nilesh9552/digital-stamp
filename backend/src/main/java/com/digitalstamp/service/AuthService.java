package com.digitalstamp.service;

import com.digitalstamp.config.AppProperties;
import com.digitalstamp.dto.auth.AuthResponse;
import com.digitalstamp.dto.auth.LoginRequest;
import com.digitalstamp.dto.auth.RefreshRequest;
import com.digitalstamp.dto.auth.RegisterRequest;
import com.digitalstamp.entity.RefreshToken;
import com.digitalstamp.entity.Role;
import com.digitalstamp.entity.User;
import com.digitalstamp.exception.ApiException;
import com.digitalstamp.repository.RefreshTokenRepository;
import com.digitalstamp.repository.UserRepository;
import com.digitalstamp.security.JwtService;
import com.digitalstamp.util.TokenUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final LoyaltyService loyaltyService;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AppProperties appProperties,
                       LoyaltyService loyaltyService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
        this.loyaltyService = loyaltyService;
    }

    @Transactional
    public AuthResponse registerCustomer(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw ApiException.conflict("An account with this email already exists");
        }
        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile().trim())
                .role(Role.CUSTOMER)
                .active(true)
                .qrToken(TokenUtil.uuid())
                .build();
        user = userRepository.save(user);
        if (request.getShopSlug() != null && !request.getShopSlug().isBlank()) {
            loyaltyService.enrollCustomer(user, request.getShopSlug());
        }
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw ApiException.unauthorized("Invalid email or password");
        }
        if (!user.isActive()) {
            throw ApiException.forbidden("Account is inactive");
        }
        refreshTokenRepository.deleteByUser(user);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> ApiException.unauthorized("Invalid token"));
        if (stored.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw ApiException.unauthorized("Expired token");
        }
        User user = stored.getUser();
        if (!user.isActive()) {
            throw ApiException.unauthorized("Account is inactive");
        }
        refreshTokenRepository.delete(stored);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshToken);
        }
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refresh = TokenUtil.uuid();
        RefreshToken token = RefreshToken.builder()
                .token(refresh)
                .user(user)
                .expiryDate(Instant.now().plusMillis(appProperties.getJwt().getRefreshExpirationMs()))
                .build();
        refreshTokenRepository.save(token);
        return AuthResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .tokenType("Bearer")
                .build();
    }
}
