package com.taskforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOrganization_Id(Long organizationId);

    List<Project> findByOrganization_IdIn(
            List<Long> organizationIds);

    boolean existsByOrganization_IdAndId(
            Long organizationId,
            Long projectId);
}