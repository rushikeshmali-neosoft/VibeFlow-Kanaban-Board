package com.vibeflow.auth.service;

import com.vibeflow.auth.dto.AuthResponse;
import com.vibeflow.auth.dto.LoginRequest;
import com.vibeflow.auth.dto.RegisterRequest;
import com.vibeflow.auth.entity.User;
import com.vibeflow.auth.repository.UserRepository;
import com.vibeflow.auth.security.JwtTokenProvider;
import com.vibeflow.common.exception.ValidationException;
import com.vibeflow.user.dto.UserDTO;
import com.vibeflow.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ValidationException("Email already registered");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("USER")
                .build();

        String token = jwtTokenProvider.generateToken(userDetails);
        return AuthResponse.of(token, user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(userDetails);

            return AuthResponse.of(token, userDetails.getUsername());
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String email) {
        return userMapper.toDTO(userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ValidationException("User not found")));
    }

    /**
     * Invalidates the supplied JWT by adding it to the database blacklist.
     * Even though the token remains cryptographically valid until its exp claim,
     * the {@link com.vibeflow.auth.security.JwtAuthenticationFilter} rejects any
     * token found in the blacklist before setting up the security context.
     *
     * @param rawToken  the full JWT string from the Authorization header
     * @param userEmail email extracted from the token (already authenticated)
     */
    @Transactional
    public void logout(String rawToken, String userEmail) {
        tokenBlacklistService.blacklist(rawToken, userEmail);
        log.info("User {} logged out; token invalidated in blacklist", userEmail);
    }
}
