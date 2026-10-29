package com.taskforge.dto;

import java.time.LocalDateTime;

import com.taskforge.entity.OrganizationRole;

public record MembershipResponse(
    Long id,
    Long organizationId,
    Long userId,
    String displayName,
    String email,
    OrganizationRole role,
    LocalDateTime createdAt
) {}