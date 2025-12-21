package com.example.cgroove.dto.auth;

import com.example.cgroove.dto.user.UserResponse;

public record AuthResponse(
        UserResponse userResponse,
        String accessToken
) {
    public static AuthResponse from(UserResponse userResponse, String accessToken) {
        return new AuthResponse(userResponse, accessToken);
    }
}