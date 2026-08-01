package com.dhruvil.api_gateway.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.io.Decoders;

import javax.crypto.SecretKey;
import java.util.List;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;


    public SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecretKey)
        );
    }

    public Claims getClaims (String token) {

        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public List<String> getRoles(String token) {
        Claims claims = getClaims(token);

        return ((List<?>) claims.get("roles"))
                .stream()
                .map(Object::toString)
                .toList();
    }

    public List<String> getPermissions(String token) {
        Claims claims = getClaims(token);

        return ((List<?>) claims.get("permissions"))
                .stream()
                .map(Object::toString)
                .toList();
    }
}