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

import com.taskforge.dto.OrganizationRequest;
import com.taskforge.dto.OrganizationResponse;
import com.taskforge.service.OrganizationService;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(
            OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // Create an organization
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationResponse createOrganization(
            @Valid @RequestBody OrganizationRequest request) {

        return organizationService.createOrganization(request);
    }

    // Get all organizations
    @GetMapping
    public List<OrganizationResponse> getAllOrganizations() {

        return organizationService.getAllOrganizations();
    }

    // Get one organization
    @GetMapping("/{id}")
    public OrganizationResponse getOrganizationById(
            @PathVariable Long id) {

        return organizationService.getOrganizationById(id);
    }

    // Update an organization
    @PutMapping("/{id}")
    public OrganizationResponse updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequest request) {

        return organizationService.updateOrganization(id, request);
    }

    // Delete an organization
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrganization(@PathVariable Long id) {

        organizationService.deleteOrganization(id);
    }
}