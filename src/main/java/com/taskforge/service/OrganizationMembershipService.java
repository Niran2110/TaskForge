package com.taskforge.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.dto.MembershipRequest;
import com.taskforge.dto.MembershipResponse;
import com.taskforge.entity.AppUser;
import com.taskforge.entity.Organization;
import com.taskforge.entity.OrganizationMembership;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.exception.ResourceConflictException;
import com.taskforge.exception.ResourceNotFoundException;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.OrganizationMembershipRepository;
import com.taskforge.repository.OrganizationRepository;

@Service
public class OrganizationMembershipService {

    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final AppUserRepository appUserRepository;

    public OrganizationMembershipService(
            OrganizationMembershipRepository membershipRepository,
            OrganizationRepository organizationRepository,
            AppUserRepository appUserRepository) {

        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> getMembers(Long organizationId) {

        requireMembership(organizationId);

        return membershipRepository.findByOrganization_Id(organizationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MembershipResponse addMember(
            Long organizationId,
            MembershipRequest request) {

        OrganizationMembership currentMembership =
                requireMembership(organizationId);

        String email = normalizeEmail(request.email());

        AppUser newUser = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No registered user exists with this email"));

        if (membershipRepository.existsByOrganization_IdAndUser_Id(
                organizationId, newUser.getId())) {
            throw new ResourceConflictException(
                    "This user is already a member of the organization");
        }

        // Owners can add admins or members.
        // Admins can add members only.
        if (currentMembership.getRole() == OrganizationRole.ADMIN) {
            if (request.role() != OrganizationRole.MEMBER) {
                throw new AccessDeniedException(
                        "Admins can add members only");
            }
        } else if (currentMembership.getRole() != OrganizationRole.OWNER) {
            throw new AccessDeniedException(
                    "Only owners and admins can add members");
        }

        if (request.role() == OrganizationRole.OWNER) {
            throw new AccessDeniedException(
                    "An additional owner cannot be assigned through this endpoint");
        }

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with id: " + organizationId));

        OrganizationMembership membership = new OrganizationMembership();
        membership.setOrganization(organization);
        membership.setUser(newUser);
        membership.setRole(request.role());

        return toResponse(membershipRepository.save(membership));
    }

    @Transactional
    public MembershipResponse changeRole(
            Long organizationId,
            Long userId,
            OrganizationRole newRole) {

        OrganizationMembership actor = requireMembership(organizationId);

        if (actor.getRole() != OrganizationRole.OWNER) {
            throw new AccessDeniedException(
                    "Only the organization owner can change member roles");
        }

        if (newRole == null || newRole == OrganizationRole.OWNER) {
            throw new IllegalArgumentException(
                    "This endpoint cannot assign the OWNER role");
        }

        OrganizationMembership target = membershipRepository
                .findByOrganization_IdAndUser_Id(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization membership not found"));

        if (target.getRole() == OrganizationRole.OWNER) {
            throw new IllegalArgumentException(
                    "The owner role cannot be changed through this endpoint");
        }

        target.setRole(newRole);

        return toResponse(membershipRepository.save(target));
    }

    @Transactional
    public void removeMember(Long organizationId, Long userId) {

        OrganizationMembership actor = requireMembership(organizationId);

        OrganizationMembership target = membershipRepository
                .findByOrganization_IdAndUser_Id(organizationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization membership not found"));

        if (target.getRole() == OrganizationRole.OWNER) {
            throw new AccessDeniedException(
                    "The owner cannot be removed through this endpoint");
        }

        if (actor.getRole() == OrganizationRole.ADMIN
                && target.getRole() != OrganizationRole.MEMBER) {
            throw new AccessDeniedException(
                    "Admins can remove members only");
        }

        if (actor.getRole() != OrganizationRole.OWNER
                && actor.getRole() != OrganizationRole.ADMIN) {
            throw new AccessDeniedException(
                    "Only owners and admins can remove members");
        }

        membershipRepository.delete(target);
    }

    @Transactional
    public void createOwnerMembership(Long organizationId) {

        String email = currentEmail();

        AppUser owner = appUserRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current user account was not found"));

        Organization organization = organizationRepository
                .findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Organization not found with id: " + organizationId));

        if (membershipRepository.existsByOrganization_IdAndUser_Id(
                organizationId, owner.getId())) {
            return;
        }

        OrganizationMembership membership = new OrganizationMembership();
        membership.setOrganization(organization);
        membership.setUser(owner);
        membership.setRole(OrganizationRole.OWNER);

        membershipRepository.save(membership);
    }

    private OrganizationMembership requireMembership(Long organizationId) {

        AppUser currentUser = appUserRepository
                .findByEmailIgnoreCase(currentEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Current user account was not found"));

        return membershipRepository
                .findByOrganization_IdAndUser_Id(
                        organizationId, currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException(
                        "You are not a member of this organization"));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new AccessDeniedException(
                    "Authentication is required");
        }

        return normalizeEmail(authentication.getName());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private MembershipResponse toResponse(
            OrganizationMembership membership) {

        return new MembershipResponse(
                membership.getId(),
                membership.getOrganization().getId(),
                membership.getUser().getId(),
                membership.getUser().getDisplayName(),
                membership.getUser().getEmail(),
                membership.getRole(),
                membership.getCreatedAt());
    }
}