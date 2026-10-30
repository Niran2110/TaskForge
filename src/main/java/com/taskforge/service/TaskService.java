package com.taskforge.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.TaskRequest;
import com.taskforge.dto.TaskResponse;
import com.taskforge.entity.AppUser;
import com.taskforge.entity.Project;
import com.taskforge.entity.Task;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final OrganizationAccessService organizationAccessService;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            AppUserRepository appUserRepository,
            OrganizationAccessService organizationAccessService) {

        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.appUserRepository = appUserRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request) {

        Project project = findProject(request.projectId());

        Long organizationId = project.getOrganization().getId();

        organizationAccessService.requireMembership(organizationId);

        AppUser assignee = findAssignee(request.assigneeId());

        if (assignee != null) {
            organizationAccessService.requireUserIsOrganizationMember(
                    organizationId,
                    assignee.getId());
        }

        Task task = new Task();

        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());
        task.setProject(project);
        task.setAssignee(assignee);

        Task savedTask = taskRepository.save(task);

        return convertToResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {

        return searchTasks(null, null, null, null);
    }
    
    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(
            Pageable pageable,
            Long projectId,
            com.taskforge.entity.TaskStatus status,
            com.taskforge.entity.TaskPriority priority,
            Long assigneeId) {

        List<Long> organizationIds =
                organizationAccessService.getAccessibleOrganizationIds();

        if (organizationIds.isEmpty()) {
            return Page.empty(pageable);
        }

        if (projectId != null) {

            Project project = findProject(projectId);

            organizationAccessService.requireMembership(
                    project.getOrganization().getId());
        }

        return taskRepository.searchTasksPaginated(
                        organizationIds,
                        projectId,
                        status,
                        priority,
                        assigneeId,
                        pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> searchTasks(
            Long projectId,
            com.taskforge.entity.TaskStatus status,
            com.taskforge.entity.TaskPriority priority,
            Long assigneeId) {

        List<Long> organizationIds =
                organizationAccessService.getAccessibleOrganizationIds();

        if (organizationIds.isEmpty()) {
            return List.of();
        }

        if (projectId != null) {

            Project project = findProject(projectId);

            organizationAccessService.requireMembership(
                    project.getOrganization().getId());
        }

        return taskRepository.searchTasks(
                        organizationIds,
                        projectId,
                        status,
                        priority,
                        assigneeId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {

        Task task = findTask(id);

        organizationAccessService.requireMembership(
                task.getProject().getOrganization().getId());

        return convertToResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {

        Task existingTask = findTask(id);

        Long existingOrganizationId =
                existingTask.getProject().getOrganization().getId();

        organizationAccessService.requireMembership(
                existingOrganizationId);

        Project project = findProject(request.projectId());

        Long requestedOrganizationId =
                project.getOrganization().getId();

        if (!existingOrganizationId.equals(requestedOrganizationId)) {
            throw new IllegalArgumentException(
                    "Moving a task to another organization's project is not allowed");
        }

        AppUser assignee = findAssignee(request.assigneeId());

        if (assignee != null) {
            organizationAccessService.requireUserIsOrganizationMember(
                    existingOrganizationId,
                    assignee.getId());
        }

        existingTask.setTitle(request.title().trim());
        existingTask.setDescription(request.description());
        existingTask.setStatus(request.status());
        existingTask.setPriority(request.priority());
        existingTask.setDueDate(request.dueDate());
        existingTask.setProject(project);
        existingTask.setAssignee(assignee);

        Task updatedTask = taskRepository.save(existingTask);

        return convertToResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {

        Task task = findTask(id);

        organizationAccessService.requireMembership(
                task.getProject().getOrganization().getId());

        taskRepository.delete(task);
    }

    private Task findTask(Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Task not found with ID: " + id));
    }

    private Project findProject(Long projectId) {

        return projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + projectId));
    }

    private AppUser findAssignee(Long assigneeId) {

        if (assigneeId == null) {
            return null;
        }

        return appUserRepository.findById(assigneeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + assigneeId));
    }

    private TaskResponse convertToResponse(Task task) {

        Long assigneeId = task.getAssignee() != null
                ? task.getAssignee().getId()
                : null;

        String assigneeName = task.getAssignee() != null
                ? task.getAssignee().getDisplayName()
                : null;

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                assigneeId,
                assigneeName,
                task.getCreatedAt(),
                task.getUpdatedAt());
    }
}