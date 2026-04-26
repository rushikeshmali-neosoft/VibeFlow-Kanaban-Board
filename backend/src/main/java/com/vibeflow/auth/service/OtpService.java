package com.vibeflow.auth.service;

import com.vibeflow.auth.entity.OtpToken;
import com.vibeflow.auth.repository.OtpTokenRepository;
import com.vibeflow.auth.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    private static final int OTP_VALIDITY_MINUTES = 10;

    @Transactional
    public String generateOtp(User user) {
        // Clear any existing OTPs for this user
        otpTokenRepository.deleteByUser(user);

        // Generate 6-digit OTP
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(otpValue);

        OtpToken otpToken = OtpToken.builder()
                .otp(otpStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES))
                .build();

        otpTokenRepository.save(otpToken);
        return otpStr;
    }

    public boolean validateOtp(String otp, User user) {
        Optional<OtpToken> tokenOpt = otpTokenRepository.findByOtpAndUser(otp, user);
        
        if (tokenOpt.isEmpty()) {
            return false;
        }

        OtpToken token = tokenOpt.get();
        if (token.isExpired()) {
            otpTokenRepository.delete(token); // Clean up expired token
            return false;
        }

        return true;
    }

    @Transactional
    public void clearOtp(User user) {
        otpTokenRepository.deleteByUser(user);
    }
}
