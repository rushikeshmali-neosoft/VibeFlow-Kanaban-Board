package com.vibeflow.auth.repository;

import com.vibeflow.auth.entity.OtpToken;
import com.vibeflow.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByOtpAndUser(String otp, User user);
    void deleteByUser(User user);
}
