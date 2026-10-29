package com.taskforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProjectRequest(

        @NotBlank(message = "Project name is required")
        @Size(max = 150, message = "Project name must not exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @NotNull(message = "Organization ID is required")
        Long organizationId

) {
}