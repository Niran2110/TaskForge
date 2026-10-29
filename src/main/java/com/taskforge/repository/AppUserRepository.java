package com.taskforge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.taskforge.entity.AppUser;

public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    boolean existsByEmailIgnoreCase(String email);

    Optional<AppUser> findByEmailIgnoreCase(String email);
}