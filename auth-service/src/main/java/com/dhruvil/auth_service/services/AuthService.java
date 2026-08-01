package com.dhruvil.auth_service.services;
import com.dhruvil.auth_service.dto.*;
import com.dhruvil.auth_service.entity.RefreshToken;
import com.dhruvil.auth_service.entity.Role;
import com.dhruvil.auth_service.entity.User;
import com.dhruvil.auth_service.entity.UserRole;
import com.dhruvil.auth_service.exception.ResourceNotFoundException;
import com.dhruvil.auth_service.exception.UserAlreadyExistsException;
import com.dhruvil.auth_service.repository.RoleRepository;
import com.dhruvil.auth_service.repository.UserRepository;
import com.dhruvil.auth_service.repository.UserRoleRepository;
import com.dhruvil.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthenticationManager authenticationManager ;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthorityService authorityService;

    public MessageResponse signup(SignupRequest request) {

        // 1. Check if user already exists
        String email = request.getEmail().trim().toLowerCase();
        String employeeId = Optional.ofNullable(request.getEmployeeId())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .orElseGet(() -> "SELF-" + UUID.randomUUID());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + email
            );
        }

        if (userRepository.existsByEmployeeId(employeeId)) {
            throw new UserAlreadyExistsException(
                    "User already exists with employeeId: " + employeeId
            );
        }

        // 2. Create User
        User user = User.builder()
                .employeeId(employeeId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .department(defaultIfBlank(request.getDepartment(), "Unassigned"))
                .designation(defaultIfBlank(request.getDesignation(), "Employee"))
                .enabled(true)
                .build();

        user = userRepository.save(user);

        Role employeeRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: EMPLOYEE"));

        userRoleRepository.save(
                UserRole.builder()
                        .user(user)
                        .role(employeeRole)
                        .build()
        );

        // 6. Return Success Response
        return MessageResponse.builder()
                .message("User registered successfully.")
                .build();
    }

    public JwtResponse login (LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

         Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        /*
         * AuthenticationManager authenticates the user by calling
         * CustomUserDetailsService, which loads the user from the database.
         *
         * We load the User entity again because JwtService and
         * RefreshTokenService currently work with the JPA User entity.
         *
         * This extra query only happens during login and keeps the
         * security layer (UserPrincipal) separate from the persistence layer.
         *
         * If needed later, this can be optimized using
         * userRepository.getReferenceById(authenticatedUserId).
         */

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + email
                        ));

        AuthorityInfo authorityInfo =
                authorityService.getAuthorities(user);

        String accessToken = jwtService.generateAccessToken(
                user,
                authorityInfo.getRoles(),
                authorityInfo.getPermissions()
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .build();
    }

    public JwtResponse refresh(String refreshToken) {

        // 1. Verify Refresh Token -> DB call
        RefreshToken storedToken =
                refreshTokenService.verifyToken(refreshToken);

        User user = storedToken.getUser();

        // 2. Load Authorities
        AuthorityInfo authorityInfo =
                authorityService.getAuthorities(user);

        // 3. Generate New Access Token
        String accessToken =
                jwtService.generateAccessToken(
                        user,
                        authorityInfo.getRoles(),
                        authorityInfo.getPermissions()
                );

        // 4. Rotate Refresh Token
        refreshTokenService.revokeToken(storedToken);

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getTokenHash())
                .build();
    }

    public void logout(String refreshToken) {
        RefreshToken storedToken =
                refreshTokenService.verifyToken(refreshToken);

        refreshTokenService.revokeToken(storedToken);
    }

    private String defaultIfBlank(String value, String fallback) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(candidate -> !candidate.isBlank())
                .orElse(fallback);
    }
}
