package com.taskforge.dto;

import java.time.LocalDateTime;

public record OrganizationResponse(
        Long id,
        String name,
        String slug,
        String description,
        LocalDateTime createdAt
) {
}