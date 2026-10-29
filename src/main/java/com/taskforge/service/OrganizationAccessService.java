package com.taskforge.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.taskforge.entity.AppUser;
import com.taskforge.entity.OrganizationMembership;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.OrganizationMembershipRepository;

@Service
@Transactional(readOnly = true)
public class OrganizationAccessService {

    private final AppUserRepository appUserRepository;
    private final OrganizationMembershipRepository membershipRepository;

    public OrganizationAccessService(
            AppUserRepository appUserRepository,
            OrganizationMembershipRepository membershipRepository) {

        this.appUserRepository = appUserRepository;
        this.membershipRepository = membershipRepository;
    }

    private AppUser getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().equals("anonymousUser")) {

            throw new AccessDeniedException("Authentication is required");
        }

        return appUserRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Authenticated user was not found"));
    }

    public OrganizationMembership requireMembership(Long organizationId) {
        AppUser currentUser = getCurrentUser();

        return membershipRepository
                .findByOrganization_IdAndUser_Id(
                        organizationId,
                        currentUser.getId())
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "You are not a member of this organization"));
    }

    public OrganizationMembership requireRole(
            Long organizationId,
            OrganizationRole... allowedRoles) {

        OrganizationMembership membership =
                requireMembership(organizationId);

        boolean allowed = Arrays.asList(allowedRoles)
                .contains(membership.getRole());

        if (!allowed) {
            throw new AccessDeniedException(
                    "You do not have permission to perform this operation");
        }

        return membership;
    }

    public List<Long> getAccessibleOrganizationIds() {
        AppUser currentUser = getCurrentUser();

        return membershipRepository
                .findByUser_Id(currentUser.getId())
                .stream()
                .map(membership -> membership.getOrganization().getId())
                .distinct()
                .toList();
    }

    public void requireUserIsOrganizationMember(
            Long organizationId,
            Long userId) {

        boolean isMember =
                membershipRepository
                        .existsByOrganization_IdAndUser_Id(
                                organizationId,
                                userId);

        if (!isMember) {
            throw new IllegalArgumentException(
                    "The assignee must be a member of the task's organization");
        }
    }
}