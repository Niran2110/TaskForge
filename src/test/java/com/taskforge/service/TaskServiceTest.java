package com.taskforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.taskforge.dto.TaskRequest;
import com.taskforge.dto.TaskResponse;
import com.taskforge.entity.AppUser;
import com.taskforge.entity.Organization;
import com.taskforge.entity.Project;
import com.taskforge.entity.Task;
import com.taskforge.entity.TaskPriority;
import com.taskforge.entity.TaskStatus;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.ProjectRepository;
import com.taskforge.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OrganizationAccessService organizationAccessService;

    @Mock
    private Project project;

    @Mock
    private Organization organization;

    @Mock
    private AppUser assignee;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    private final Long taskId = 10L;
    private final Long projectId = 20L;
    private final Long organizationId = 1L;
    private final Long anotherOrganizationId = 2L;
    private final Long assigneeId = 30L;

    @BeforeEach
    void setUp() {
        lenient().when(project.getId()).thenReturn(projectId);

        lenient().when(project.getOrganization())
                .thenReturn(organization);

        lenient().when(organization.getId())
                .thenReturn(organizationId);

        task = new Task();
        task.setTitle("Original Task");
        task.setDescription("Original description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(project);
    }

    private TaskRequest createRequest(
            String title,
            String description,
            Long requestedProjectId,
            Long requestedAssigneeId) {

        return new TaskRequest(
                title,
                description,
                TaskStatus.TODO,
                TaskPriority.MEDIUM,
                null,
                requestedProjectId,
                requestedAssigneeId
        );
    }

    // Test 1: Create a task successfully.

    @Test
    void createTask_shouldCreateTaskSuccessfully() {

        TaskRequest request = createRequest(
                "  New Task  ",
                "Task description",
                projectId,
                null
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(request);

        assertEquals("New Task", response.title());
        assertEquals("Task description", response.description());
        assertEquals(projectId, response.projectId());
        assertEquals(TaskStatus.TODO, response.status());
        assertEquals(TaskPriority.MEDIUM, response.priority());

        verify(organizationAccessService)
                .requireMembership(organizationId);

        verify(taskRepository).save(any(Task.class));
    }

    // Test 2: Reject assigning a task to a user who is not
    // a member of the project's organization.

    @Test
    void createTask_shouldRejectAssigneeOutsideOrganization() {

        TaskRequest request = createRequest(
                "New Task",
                "Task description",
                projectId,
                assigneeId
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(appUserRepository.findById(assigneeId))
                .thenReturn(Optional.of(assignee));

        when(assignee.getId()).thenReturn(assigneeId);

        doThrow(new AccessDeniedException(
                "Assignee is not a member of this organization"))
                .when(organizationAccessService)
                .requireUserIsOrganizationMember(
                        organizationId,
                        assigneeId
                );

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.createTask(request)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    // Test 3: Deny viewing a task without organization membership.

    @Test
    void getTaskById_shouldDenyUserWithoutMembership() {

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        doThrow(new AccessDeniedException(
                "You are not a member of this organization"))
                .when(organizationAccessService)
                .requireMembership(organizationId);

        assertThrows(
                AccessDeniedException.class,
                () -> taskService.getTaskById(taskId)
        );
    }

    // Test 4: Successfully update a task.

    @Test
    void updateTask_shouldUpdateTaskSuccessfully() {

        TaskRequest request = createRequest(
                "Updated Task",
                "Updated description",
                projectId,
                null
        );

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response =
                taskService.updateTask(taskId, request);

        assertEquals("Updated Task", response.title());
        assertEquals("Updated description", response.description());
        assertEquals(projectId, response.projectId());

        verify(organizationAccessService)
                .requireMembership(organizationId);

        verify(taskRepository).save(task);
    }

    // Test 5: Prevent moving a task to another organization's project.

    @Test
    void updateTask_shouldRejectMovingTaskAcrossOrganizations() {

        Project anotherProject = org.mockito.Mockito.mock(Project.class);

        Organization anotherOrganization =
                org.mockito.Mockito.mock(Organization.class);

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        when(projectRepository.findById(projectId + 1))
                .thenReturn(Optional.of(anotherProject));

        when(anotherProject.getOrganization())
                .thenReturn(anotherOrganization);

        when(anotherOrganization.getId())
                .thenReturn(anotherOrganizationId);

        TaskRequest request = createRequest(
                "Updated Task",
                "Updated description",
                projectId + 1,
                null
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> taskService.updateTask(taskId, request)
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    // Test 6: Successfully delete a task.

    @Test
    void deleteTask_shouldDeleteTaskSuccessfully() {

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(organizationAccessService)
                .requireMembership(organizationId);

        verify(taskRepository).delete(task);
    }

    // Test 7: Return an empty list when the user has no organizations.

    @Test
    void searchTasks_shouldReturnEmptyListWithoutAccessibleOrganizations() {

        when(organizationAccessService.getAccessibleOrganizationIds())
                .thenReturn(List.of());

        List<TaskResponse> responses = taskService.searchTasks(
                null,
                null,
                null,
                null
        );

        assertEquals(0, responses.size());

        verify(taskRepository, never()).searchTasks(
                org.mockito.ArgumentMatchers.<Long>anyList(),
                org.mockito.ArgumentMatchers.nullable(Long.class),
                org.mockito.ArgumentMatchers.nullable(TaskStatus.class),
                org.mockito.ArgumentMatchers.nullable(TaskPriority.class),
                org.mockito.ArgumentMatchers.nullable(Long.class)
        );
    }

    // Test 8: Throw an exception when the task does not exist.

    @Test
    void getTaskById_shouldThrowWhenTaskDoesNotExist() {

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> taskService.getTaskById(taskId)
        );
    }
}