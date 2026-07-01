package com.dhruvil.auth_service.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(BEARER.length());

        try {

            // check if token valid or not
            Claims claims = jwtService.extractAllClaims(jwt);

            Long userId = claims.get("userId", Long.class);

            String email = claims.getSubject();

            Collection<SimpleGrantedAuthority> authorities = buildAuthorities(claims);

            UserPrincipal principal = new UserPrincipal(
                    userId,
                    email,
                    "",
                    true,
                    authorities
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

            log.info("JWT Success");
        } catch (JwtException ex) {
            log.info("Some error occurss : JWT token is invalid");
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    @SuppressWarnings("unchecked")
    private Collection<SimpleGrantedAuthority> buildAuthorities(
            Claims claims) {

        Collection<SimpleGrantedAuthority> authorities = new HashSet<>();

        List<String> roles =
                (List<String>) claims.get("roles");

        List<String> permissions =
                (List<String>) claims.get("permissions");

        if (roles != null) {
            roles.forEach(role ->
                    authorities.add(
                            new SimpleGrantedAuthority("ROLE_" + role)
                    ));
        }

        if (permissions != null) {
            permissions.forEach(permission ->
                    authorities.add(
                            new SimpleGrantedAuthority(permission)
                    ));
        }

        return authorities;
    }
}