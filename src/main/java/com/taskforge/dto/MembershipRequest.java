package com.taskforge.dto;

import com.taskforge.entity.OrganizationRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembershipRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    String email,

    @NotNull(message = "Role is required")
    OrganizationRole role

) {}