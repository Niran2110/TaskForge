package com.taskforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.taskforge.entity.Task;
import com.taskforge.entity.TaskPriority;
import com.taskforge.entity.TaskStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
            SELECT t
            FROM Task t
            WHERE t.project.organization.id IN :organizationIds
              AND (:projectId IS NULL OR t.project.id = :projectId)
              AND (:status IS NULL OR t.status = :status)
              AND (:priority IS NULL OR t.priority = :priority)
              AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
            ORDER BY t.createdAt DESC
            """)
    List<Task> searchTasks(
            @Param("organizationIds") List<Long> organizationIds,
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("assigneeId") Long assigneeId);

    List<Task> findByProject_Id(Long projectId);
    
    @Query(
    	    value = """
    	        SELECT t
    	        FROM Task t
    	        WHERE t.project.organization.id IN :organizationIds
    	          AND (:projectId IS NULL OR t.project.id = :projectId)
    	          AND (:status IS NULL OR t.status = :status)
    	          AND (:priority IS NULL OR t.priority = :priority)
    	          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
    	        """,
    	    countQuery = """
    	        SELECT COUNT(t)
    	        FROM Task t
    	        WHERE t.project.organization.id IN :organizationIds
    	          AND (:projectId IS NULL OR t.project.id = :projectId)
    	          AND (:status IS NULL OR t.status = :status)
    	          AND (:priority IS NULL OR t.priority = :priority)
    	          AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)
    	        """
    	)
    	Page<Task> searchTasksPaginated(
    	        @Param("organizationIds") List<Long> organizationIds,
    	        @Param("projectId") Long projectId,
    	        @Param("status") TaskStatus status,
    	        @Param("priority") TaskPriority priority,
    	        @Param("assigneeId") Long assigneeId,
    	        Pageable pageable
    	);
}