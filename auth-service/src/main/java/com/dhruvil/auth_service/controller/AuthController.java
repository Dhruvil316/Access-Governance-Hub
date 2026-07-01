package com.dhruvil.auth_service.controller;

import com.dhruvil.auth_service.dto.JwtResponse;
import com.dhruvil.auth_service.dto.LoginRequest;
import com.dhruvil.auth_service.dto.MessageResponse;
import com.dhruvil.auth_service.dto.SignupRequest;
import com.dhruvil.auth_service.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


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

//    @PostMapping("/refresh")
//    public ResponseEntity<JwtResponse> refreshToken(
//            @RequestBody String refreshToken
//    ) {
//
//        JwtResponse response = authService.refreshToken(refreshToken);
//
//        return ResponseEntity.ok(response);
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(
//            @RequestBody String refreshToken
//    ) {
//        authService.logout(refreshToken);
//        return ResponseEntity.noContent().build();
//    }

}
