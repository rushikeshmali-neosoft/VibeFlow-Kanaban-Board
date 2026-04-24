package com.vibeflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String email;
    
    public static AuthResponse of(String token, String email) {
        return new AuthResponse(token, email);
    }
}