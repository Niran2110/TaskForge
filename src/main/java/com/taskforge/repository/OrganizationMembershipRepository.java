package com.taskforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.entity.OrganizationMembership;

public interface OrganizationMembershipRepository
        extends JpaRepository<OrganizationMembership, Long> {

    boolean existsByOrganization_IdAndUser_Id(
            Long organizationId,
            Long userId);

    Optional<OrganizationMembership>
    findByOrganization_IdAndUser_Id(
            Long organizationId,
            Long userId);

    List<OrganizationMembership> findByOrganization_Id(
            Long organizationId);

    List<OrganizationMembership> findByUser_Id(
            Long userId);
    
}