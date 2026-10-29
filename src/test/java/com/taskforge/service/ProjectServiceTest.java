package com.taskforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.taskforge.dto.ProjectRequest;
import com.taskforge.dto.ProjectResponse;
import com.taskforge.entity.Organization;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.entity.Project;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.OrganizationRepository;
import com.taskforge.repository.ProjectRepository;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationAccessService organizationAccessService;

    @Mock
    private Organization organization;

    @InjectMocks
    private ProjectService projectService;

    private Project project;

    private final Long projectId = 10L;
    private final Long organizationId = 1L;

    @BeforeEach
    void setUp() {
        project = new Project(
                "TaskForge Backend",
                "Backend development project"
        );

        project.setOrganization(organization);

        lenient().when(organization.getId()).thenReturn(organizationId);
    }

    // Test 1: OWNER can update a project.

    @Test
    void updateProject_shouldAllowOwner() {

        ProjectRequest request = new ProjectRequest(
                "Updated Project",
                "Updated description",
                organizationId
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjectResponse response =
                projectService.updateProject(projectId, request);

        assertEquals("Updated Project", project.getName());
        assertEquals("Updated description", project.getDescription());

        assertEquals("Updated Project", response.name());

        verify(organizationAccessService).requireRole(
                eq(organizationId),
                eq(OrganizationRole.OWNER),
                eq(OrganizationRole.ADMIN)
        );

        verify(projectRepository).save(project);
    }

    // Test 2: ADMIN can delete a project.

    @Test
    void deleteProject_shouldAllowAdmin() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        projectService.deleteProject(projectId);

        verify(organizationAccessService).requireRole(
                eq(organizationId),
                eq(OrganizationRole.OWNER),
                eq(OrganizationRole.ADMIN)
        );

        verify(projectRepository).delete(project);
    }

    // Test 3: MEMBER cannot update a project when access is denied.

    @Test
    void updateProject_shouldDenyMember() {

        ProjectRequest request = new ProjectRequest(
                "Updated Project",
                "Updated description",
                organizationId
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        doThrow(new AccessDeniedException(
                "Insufficient permissions to perform this action"))
                .when(organizationAccessService)
                .requireRole(
                        eq(organizationId),
                        eq(OrganizationRole.OWNER),
                        eq(OrganizationRole.ADMIN)
                );

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.updateProject(projectId, request)
        );

        // The project must not be saved after access is denied.
        verify(projectRepository, never()).save(any(Project.class));
    }

    // Test 4: MEMBER cannot delete a project when access is denied.

    @Test
    void deleteProject_shouldDenyMember() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        doThrow(new AccessDeniedException(
                "Insufficient permissions to perform this action"))
                .when(organizationAccessService)
                .requireRole(
                        eq(organizationId),
                        eq(OrganizationRole.OWNER),
                        eq(OrganizationRole.ADMIN)
                );

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.deleteProject(projectId)
        );

        // The project must not be deleted after access is denied.
        verify(projectRepository, never()).delete(any(Project.class));
    }

    // Test 5: A user without organization membership cannot view a project.

    @Test
    void getProjectById_shouldDenyUserWithoutMembership() {

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        doThrow(new AccessDeniedException(
                "You are not a member of this organization"))
                .when(organizationAccessService)
                .requireMembership(organizationId);

        assertThrows(
                AccessDeniedException.class,
                () -> projectService.getProjectById(projectId)
        );
    }

    // Test 6: A project cannot be moved to another organization.

    @Test
    void updateProject_shouldRejectMovingProjectToAnotherOrganization() {

        Long anotherOrganizationId = 2L;

        ProjectRequest request = new ProjectRequest(
                "Updated Project",
                "Updated description",
                anotherOrganizationId
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.of(project));

        assertThrows(
                IllegalArgumentException.class,
                () -> projectService.updateProject(projectId, request)
        );

        verify(projectRepository, never()).save(any(Project.class));
    }

    // Test 7: Updating a nonexistent project should fail.

    @Test
    void updateProject_shouldThrowWhenProjectDoesNotExist() {

        ProjectRequest request = new ProjectRequest(
                "Updated Project",
                "Updated description",
                organizationId
        );

        when(projectRepository.findById(projectId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.updateProject(projectId, request)
        );
    }
}