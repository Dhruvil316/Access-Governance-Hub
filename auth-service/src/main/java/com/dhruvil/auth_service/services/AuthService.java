package com.dhruvil.auth_service.services;

import com.dhruvil.auth_service.dto.AuthorityInfo;
import com.dhruvil.auth_service.dto.JwtResponse;
import com.dhruvil.auth_service.dto.LoginRequest;
import com.dhruvil.auth_service.dto.SignupRequest;
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
import com.dhruvil.auth_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Objects;

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


    public JwtResponse signup(SignupRequest request) {

        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email : " + request.getEmail()
            );
        }

        // 2. Create User
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        user = userRepository.save(user);

        // 3. Assign Default Role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() ->
                        new ResourceNotFoundException("Default role USER not found"));

        UserRole mapping = UserRole.builder()
                .user(user)
                .role(userRole)
                .build();

        userRoleRepository.save(mapping);

        // 4. Load Roles & Permissions
        AuthorityInfo authorityInfo =
                authorityService.getAuthorities(user);

        // 5. Generate Access Token
        String accessToken = jwtService.generateAccessToken(
                user,
                authorityInfo.getRoles(),
                authorityInfo.getPermissions()
        );

        // 6. Generate & Save Refresh Token
        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        // 7. Return Response
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash()) // currently storing raw token
                .build();
    }

    public JwtResponse login (LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal() ;
        Collection<? extends GrantedAuthority> authorities =
                userPrincipal.getAuthorities();

        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(role -> role.substring(5))
                .toList();

        List<String> permissions = authorities.stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .toList();

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with email: " + request.getEmail()
                        ));


        String accessToken = jwtService.generateAccessToken(
                user,
                roles ,
                permissions
        );

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getTokenHash())
                .build();
    }
}
