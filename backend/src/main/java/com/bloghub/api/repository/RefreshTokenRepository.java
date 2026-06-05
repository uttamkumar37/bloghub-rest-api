package com.bloghub.api.repository;

import com.bloghub.api.entity.RefreshToken;
import com.bloghub.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByUserAndExpiresAtBefore(User user, LocalDateTime expiresAt);
}
