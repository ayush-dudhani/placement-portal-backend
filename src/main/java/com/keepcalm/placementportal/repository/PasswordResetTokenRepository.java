package com.keepcalm.placementportal.repository;

import com.keepcalm.placementportal.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenHashAndPurpose(String tokenHash, PasswordResetToken.Purpose purpose);
}
