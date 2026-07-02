package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.config.JwtProperties;
import com.dhruvil.auth_service.constants.CookieConstants;
import com.dhruvil.auth_service.dto.*;
import com.dhruvil.auth_service.exception.InvalidRefreshTokenException;
import com.dhruvil.auth_service.services.AuthService;
import com.dhruvil.auth_service.utils.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final JwtProperties jwtProperties ;

    @PostMapping("/signup")
    public ResponseEntity<MessageResponse> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        MessageResponse response = authService.signup(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        // Log all cookies received
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            log.info("Received {} cookies", cookies.length);
            for (Cookie c : cookies) {
                log.info(
                        "Cookie -> Name: {}, Value: {}, Domain: {}, Path: {}, MaxAge: {}, Secure: {}, HttpOnly: {}",
                        c.getName(),
                        c.getValue(),      // ⚠️ Avoid this in production
                        c.getDomain(),
                        c.getPath(),
                        c.getMaxAge(),
                        c.getSecure(),
                        c.isHttpOnly()
                );
            }
        } else {
            log.info("No cookies received in logout request.");
        }

        Cookie cookie = CookieUtil.getCookie(
                request,
                CookieConstants.REFRESH_TOKEN
        ).orElseThrow(() ->
                new InvalidRefreshTokenException(
                        "Refresh token cookie not found."
                ));
        log.info("Refresh Token: {}", cookie.getValue()); // ⚠️ Development only

        authService.logout(cookie.getValue());

        ResponseCookie deleteCookie = ResponseCookie.from(
                        CookieConstants.REFRESH_TOKEN,
                        ""
                )
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/auth")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                deleteCookie.toString()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Logged out successfully.",
                        null
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Cookie refreshCookie = CookieUtil.getCookie(
                request,
                CookieConstants.REFRESH_TOKEN
        ).orElseThrow(() ->
                new InvalidRefreshTokenException(
                        "Refresh token cookie not found."
                )
        );

        JwtResponse jwtResponse =
                authService.refresh(refreshCookie.getValue());

        ResponseCookie cookie = ResponseCookie
                .from(
                        CookieConstants.REFRESH_TOKEN,
                        jwtResponse.getRefreshToken()
                )
                .httpOnly(true)
                .secure(false)      // true in production (HTTPS)
                .sameSite("Strict") // change to "None" if frontend is on another domain
                .path("/auth")
                .maxAge(Duration.ofMillis(
                        jwtProperties.getRefreshTokenExpiration()
                ))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Access token refreshed successfully.",
                        jwtResponse
                )
        );
    }
}
