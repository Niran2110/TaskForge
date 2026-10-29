package com.taskforge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.taskforge.entity.TaskPriority;
import com.taskforge.entity.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long projectId,
        Long assigneeId,
        String assigneeName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}