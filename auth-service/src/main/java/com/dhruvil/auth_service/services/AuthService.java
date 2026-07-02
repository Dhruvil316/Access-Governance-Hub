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
import com.dhruvil.auth_service.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + request.getEmail()
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

        // 3. Prepare Roles
        Set<String> requestedRoles = Optional.ofNullable(request.getRoles())
                .orElse(List.of("USER"))
                .stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());

        if (requestedRoles.isEmpty()) {
            requestedRoles = Set.of("USER");
        }

        // 4. Create UserRole mappings
        List<UserRole> mappings = new ArrayList<>();

        for (String roleName : requestedRoles) {

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Role not found: " + roleName
                            ));

            mappings.add(
                    UserRole.builder()
                            .user(user)
                            .role(role)
                            .build()
            );
        }

        // 5. Save Role Mappings
        userRoleRepository.saveAll(mappings);

        // 6. Return Success Response
        return MessageResponse.builder()
                .message("User registered successfully.")
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
         * userRepository.getReferenceById(userPrincipal.getId()).
         */

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
}
