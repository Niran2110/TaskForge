package com.taskforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.entity.Project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganization_Id(Long organizationId);

    List<Project> findByOrganization_IdIn(
            List<Long> organizationIds);

    boolean existsByOrganization_IdAndId(
            Long organizationId,
            Long projectId);
    
    Page<Project> findByOrganization_IdIn(
            List<Long> organizationIds,
            Pageable pageable
    );

    Page<Project> findByOrganization_Id(
            Long organizationId,
            Pageable pageable
    );
}