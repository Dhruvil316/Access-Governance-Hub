package com.dhruvil.auth_service.repository;

import com.dhruvil.auth_service.entity.RefreshToken;
import com.dhruvil.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    boolean existsByTokenHash(String tokenHash);
}