package com.taskforge.dto;

import com.taskforge.entity.OrganizationRole;
import jakarta.validation.constraints.NotNull;

public record RoleChangeRequest(

    @NotNull(message = "Role is required")
    OrganizationRole role

) {}