package com.taskforge.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.OrganizationRequest;
import com.taskforge.dto.OrganizationResponse;
import com.taskforge.entity.Organization;
import com.taskforge.exception.ResourceConflictException;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.OrganizationRepository;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipService membershipService;

    public OrganizationService(
            OrganizationRepository organizationRepository, OrganizationMembershipService membershipService) {
        this.organizationRepository = organizationRepository;
        this.membershipService = membershipService;
    }

    // Create an organization
    public OrganizationResponse createOrganization(
            OrganizationRequest request) {

        if (organizationRepository.existsBySlug(request.slug())) {
            throw new ResourceConflictException(
                    "An organization with slug '" + request.slug()
                            + "' already exists."
            );
        }

        Organization organization = new Organization(
                request.name(),
                request.slug(),
                request.description()
        );

        Organization savedOrganization =
                organizationRepository.save(organization);
        
        membershipService.createOwnerMembership(savedOrganization.getId());

        return convertToResponse(savedOrganization);
    }

    // Get all organizations
    public List<OrganizationResponse> getAllOrganizations() {

        return organizationRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get an organization by ID
    public OrganizationResponse getOrganizationById(Long id) {

        Organization organization = findOrganizationEntity(id);

        return convertToResponse(organization);
    }

    // Update an organization
    @Transactional
    public OrganizationResponse updateOrganization(
            Long id,
            OrganizationRequest request) {

        Organization existingOrganization = findOrganizationEntity(id);

        boolean slugChanged =
                !existingOrganization.getSlug().equals(request.slug());

        if (slugChanged
                && organizationRepository.existsBySlugAndIdNot(
                        request.slug(), id)) {

            throw new ResourceConflictException(
                    "An organization with slug '" + request.slug()
                            + "' already exists."
            );
        }

        existingOrganization.setName(request.name());
        existingOrganization.setSlug(request.slug());
        existingOrganization.setDescription(request.description());

        Organization updatedOrganization =
                organizationRepository.save(existingOrganization);

        return convertToResponse(updatedOrganization);
    }

    // Delete an organization
    public void deleteOrganization(Long id) {

        Organization organization = findOrganizationEntity(id);

        organizationRepository.delete(organization);
    }

    // Find organization or throw 404 exception
    private Organization findOrganizationEntity(Long id) {

        return organizationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Organization not found with ID: " + id
                        )
                );
    }

    // Convert entity to response DTO
    private OrganizationResponse convertToResponse(
            Organization organization) {

        return new OrganizationResponse(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getDescription(),
                organization.getCreatedAt()
        );
    }
}