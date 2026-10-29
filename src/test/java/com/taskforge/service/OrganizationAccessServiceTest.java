package com.taskforge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.taskforge.entity.AppUser;
import com.taskforge.entity.Organization;
import com.taskforge.entity.OrganizationMembership;
import com.taskforge.entity.OrganizationRole;
import com.taskforge.repository.AppUserRepository;
import com.taskforge.repository.OrganizationMembershipRepository;

@ExtendWith(MockitoExtension.class)
class OrganizationAccessServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private OrganizationMembershipRepository membershipRepository;

    private OrganizationAccessService organizationAccessService;

    private AppUser currentUser;

    @BeforeEach
    void setUp() {
        organizationAccessService = new OrganizationAccessService(
                appUserRepository,
                membershipRepository
        );

        currentUser = mock(AppUser.class);

        when(currentUser.getId()).thenReturn(10L);

        // Simulate a request authenticated with this user's email.
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        "user@example.com",
                        null,
                        List.of()
                )
        );

        when(appUserRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(currentUser));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireMembership_WhenUserIsMember_ReturnsMembership() {
        OrganizationMembership membership = mock(OrganizationMembership.class);

        when(membershipRepository.findByOrganization_IdAndUser_Id(100L, 10L))
                .thenReturn(Optional.of(membership));

        OrganizationMembership result =
                organizationAccessService.requireMembership(100L);

        assertSame(membership, result);

        verify(membershipRepository)
                .findByOrganization_IdAndUser_Id(100L, 10L);
    }

    @Test
    void requireMembership_WhenUserIsNotMember_ThrowsAccessDeniedException() {
        when(membershipRepository.findByOrganization_IdAndUser_Id(200L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> organizationAccessService.requireMembership(200L)
        );
    }

    @Test
    void requireRole_WhenUserHasAllowedRole_ReturnsMembership() {
        OrganizationMembership membership = mock(OrganizationMembership.class);

        when(membershipRepository.findByOrganization_IdAndUser_Id(100L, 10L))
                .thenReturn(Optional.of(membership));

        when(membership.getRole()).thenReturn(OrganizationRole.ADMIN);

        OrganizationMembership result =
                organizationAccessService.requireRole(
                        100L,
                        OrganizationRole.OWNER,
                        OrganizationRole.ADMIN
                );

        assertSame(membership, result);
    }

    @Test
    void requireRole_WhenUserDoesNotHaveAllowedRole_ThrowsAccessDeniedException() {
        OrganizationMembership membership = mock(OrganizationMembership.class);

        when(membershipRepository.findByOrganization_IdAndUser_Id(100L, 10L))
                .thenReturn(Optional.of(membership));

        when(membership.getRole()).thenReturn(OrganizationRole.MEMBER);

        assertThrows(
                AccessDeniedException.class,
                () -> organizationAccessService.requireRole(
                        100L,
                        OrganizationRole.OWNER,
                        OrganizationRole.ADMIN
                )
        );
    }

    @Test
    void getAccessibleOrganizationIds_ReturnsOnlyUserOrganizationIds() {
        OrganizationMembership firstMembership =
                mock(OrganizationMembership.class);

        OrganizationMembership secondMembership =
                mock(OrganizationMembership.class);

        Organization firstOrganization = mock(Organization.class);
        Organization secondOrganization = mock(Organization.class);

        when(membershipRepository.findByUser_Id(10L))
                .thenReturn(List.of(firstMembership, secondMembership));

        when(firstMembership.getOrganization()).thenReturn(firstOrganization);
        when(secondMembership.getOrganization()).thenReturn(secondOrganization);

        when(firstOrganization.getId()).thenReturn(100L);
        when(secondOrganization.getId()).thenReturn(200L);

        List<Long> result =
                organizationAccessService.getAccessibleOrganizationIds();

        assertEquals(List.of(100L, 200L), result);
    }
}
