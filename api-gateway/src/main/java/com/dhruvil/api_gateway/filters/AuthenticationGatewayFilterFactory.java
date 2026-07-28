package com.dhruvil.api_gateway.filters;

import com.dhruvil.api_gateway.services.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

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

//            Long userId = jwtService.getUserIdFromToken(token) ;

            try {
                Long userId = jwtService.getUserIdFromToken(token);
                exchange.getRequest()
                        .mutate()
                        .header("X-User-Id" , userId.toString() )
                        .build() ;

                return chain.filter(exchange);
            } catch (JwtException e) {

                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();

            }

        };
    }

    public static class Config{}
}
