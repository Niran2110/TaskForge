package com.taskforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(

        @NotBlank(message = "Organization name is required")
        @Size(max = 150, message = "Organization name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Organization slug is required")
        @Size(max = 100, message = "Organization slug must not exceed 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must contain lowercase letters, numbers, and single hyphens only"
        )
        String slug,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description

) {
}