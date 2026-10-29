package com.taskforge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.entity.Organization;

public interface OrganizationRepository
        extends JpaRepository<Organization, Long> {

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);
}