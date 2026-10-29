package com.taskforge.dto;

import java.time.LocalDateTime;

public record ProjectResponse(
        Long id,
        String name,
        String description,
        Long organizationId,
        LocalDateTime createdAt
) {
}