package com.taskforge.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.taskforge.entity.TaskPriority;
import com.taskforge.entity.TaskStatus;

public record TaskRequest(

        @NotBlank(message = "Task title is required")
        @Size(max = 200, message = "Task title must not exceed 200 characters")
        String title,

        @Size(max = 2000, message = "Task description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Task status is required")
        TaskStatus status,

        @NotNull(message = "Task priority is required")
        TaskPriority priority,

        LocalDate dueDate,

        @NotNull(message = "Project ID is required")
        Long projectId,

        Long assigneeId

) {
}