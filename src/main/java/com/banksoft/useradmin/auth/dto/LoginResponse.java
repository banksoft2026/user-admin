package com.banksoft.useradmin.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UUID userId;
    private String username;
    private String fullName;
    private String email;
    private List<String> roles;
    private boolean mustChangePassword;
}
