package com.taskforge.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.ProjectResponse;
import com.taskforge.service.ProjectService;

@RestController
@RequestMapping("/api/organizations/{organizationId}/projects")
public class OrganizationProjectController {

    private final ProjectService projectService;

    public OrganizationProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public List<ProjectResponse> getProjectsByOrganization(
            @PathVariable Long organizationId) {

        return projectService.getProjectsByOrganization(organizationId);
    }
}