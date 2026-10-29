package com.taskforge.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.ProjectRequest;
import com.taskforge.dto.ProjectResponse;
import com.taskforge.entity.Organization;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.entity.Project;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.OrganizationRepository;
import com.taskforge.repository.ProjectRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAccessService organizationAccessService;

    public ProjectService(
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            OrganizationAccessService organizationAccessService) {

        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.organizationAccessService = organizationAccessService;
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {

        Long organizationId = request.organizationId();

        organizationAccessService.requireRole(
                organizationId,
                OrganizationRole.OWNER,
                OrganizationRole.ADMIN);

        Organization organization = findOrganization(organizationId);

        Project project = new Project(
                request.name(),
                request.description());

        project.setOrganization(organization);

        Project savedProject = projectRepository.save(project);

        return convertToResponse(savedProject);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {

        List<Long> organizationIds =
                organizationAccessService.getAccessibleOrganizationIds();

        if (organizationIds.isEmpty()) {
            return List.of();
        }

        return projectRepository
                .findByOrganization_IdIn(organizationIds)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByOrganization(
            Long organizationId) {

        organizationAccessService.requireMembership(organizationId);

        findOrganization(organizationId);

        return projectRepository
                .findByOrganization_Id(organizationId)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {

        Project project = findProjectEntity(id);

        organizationAccessService.requireMembership(
                project.getOrganization().getId());

        return convertToResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(
            Long id,
            ProjectRequest request) {

        Project existingProject = findProjectEntity(id);

        Long existingOrganizationId =
                existingProject.getOrganization().getId();

        organizationAccessService.requireRole(
                existingOrganizationId,
                OrganizationRole.OWNER,
                OrganizationRole.ADMIN);

        if (!existingOrganizationId.equals(request.organizationId())) {
            throw new IllegalArgumentException(
                    "Moving a project to another organization is not allowed");
        }

        existingProject.setName(request.name());
        existingProject.setDescription(request.description());

        Project updatedProject =
                projectRepository.save(existingProject);

        return convertToResponse(updatedProject);
    }

    @Transactional
    public void deleteProject(Long id) {

        Project project = findProjectEntity(id);

        organizationAccessService.requireRole(
                project.getOrganization().getId(),
                OrganizationRole.OWNER,
                OrganizationRole.ADMIN);

        projectRepository.delete(project);
    }

    private Project findProjectEntity(Long id) {

        return projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + id));
    }

    private Organization findOrganization(Long organizationId) {

        return organizationRepository.findById(organizationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organization not found with ID: "
                                        + organizationId));
    }

    private ProjectResponse convertToResponse(Project project) {

        Long organizationId = project.getOrganization() != null
                ? project.getOrganization().getId()
                : null;

        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                organizationId,
                project.getCreatedAt());
    }
}