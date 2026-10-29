package com.taskforge.dto;

public record AuthResponse(
    String tokenType,
    String accessToken,
    long expiresIn,
    Long userId,
    String displayName,
    String email
) {}