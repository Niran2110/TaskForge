package com.taskforge.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.ProjectRequest;
import com.taskforge.dto.ProjectResponse;
import com.taskforge.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Create a project
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(
            @Valid @RequestBody ProjectRequest request) {

        return projectService.createProject(request);
    }

    // Get all projects
    @GetMapping
    public List<ProjectResponse> getAllProjects() {

        return projectService.getAllProjects();
    }

    // Get a project by ID
    @GetMapping("/{id}")
    public ProjectResponse getProjectById(
            @PathVariable Long id) {

        return projectService.getProjectById(id);
    }

    // Update a project
    @PutMapping("/{id}")
    public ProjectResponse updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequest request) {

        return projectService.updateProject(id, request);
    }

    // Delete a project
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long id) {

        projectService.deleteProject(id);
    }
}