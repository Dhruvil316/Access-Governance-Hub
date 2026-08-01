package com.dhruvil.api_gateway.filters;

import com.dhruvil.api_gateway.services.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final JwtService jwtService ;

    public AuthenticationGatewayFilterFactory (JwtService jwtService) {
        super(Config.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange,chain) -> {
            String authorisationHeader = exchange.getRequest().getHeaders().getFirst("Authorization") ;

            if (authorisationHeader == null || !authorisationHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authorisationHeader.replace("Bearer ", "");

            try {
                Claims claims = jwtService.getClaims(token) ;
                Long userId = claims.get("userId", Long.class);
                String email = claims.getSubject();
                List<String> roles = jwtService.getRoles(token);
                List<String> permissions = jwtService.getPermissions(token);

                ServerHttpRequest request =
                        exchange.getRequest()
                                .mutate()

                                // Remove any client supplied values
                                .headers(headers -> {
                                    headers.remove("X-User-Id");
                                    headers.remove("X-Email");
                                    headers.remove("X-Roles");
                                    headers.remove("X-Permissions");
                                })

                                // Add trusted values
                                .header("X-User-Id", userId.toString())
                                .header("X-Email", email)
                                .header("X-Roles", String.join(",", roles))
                                .header("X-Permissions", String.join(",", permissions))

                                .build();

                return chain.filter(
                        exchange.mutate()
                                .request(request)
                                .build());

            } catch (JwtException e) {

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();

            }

        };
    }

    public static class Config{}
}
