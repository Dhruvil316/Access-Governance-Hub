package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.config.JwtProperties;
import com.dhruvil.auth_service.entity.RefreshToken;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.exception.InvalidRefreshTokenException;
import com.dhruvil.auth_service.repository.RefreshTokenRepository;
import com.dhruvil.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties ;

    public RefreshToken createRefreshToken(User user) {

        String token = jwtService.generateRefreshToken(user);

        // for now we are storing the token as it is no hash
        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(token)
                .user(user)
                .expiryDate(
                        LocalDateTime.now()
                                .plus(Duration.ofMillis(
                                        jwtProperties.getRefreshTokenExpiration()
                                ))
                )
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByTokenHash(token)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException("Refresh token not found"));
    }

    @Transactional(readOnly = true)
    public RefreshToken verifyToken(String token) {
        RefreshToken refreshToken = findByToken(token);

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new InvalidRefreshTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    public void revokeToken (String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.findByUser(user)
                .forEach(token -> token.setRevoked(true));
    }

    public void deleteAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

}