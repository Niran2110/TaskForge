package com.taskforge.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.dto.MembershipRequest;
import com.taskforge.dto.MembershipResponse;
import com.taskforge.dto.RoleChangeRequest;
import com.taskforge.service.OrganizationMembershipService;

@RestController
@RequestMapping("/api/organizations/{organizationId}/members")
public class OrganizationMembershipController {

    private final OrganizationMembershipService membershipService;

    public OrganizationMembershipController(
            OrganizationMembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public ResponseEntity<List<MembershipResponse>> getMembers(
            @PathVariable Long organizationId) {

        return ResponseEntity.ok(
                membershipService.getMembers(organizationId));
    }

    @PostMapping
    public ResponseEntity<MembershipResponse> addMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody MembershipRequest request) {

        MembershipResponse response =
                membershipService.addMember(organizationId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{userId}/role")
    public ResponseEntity<MembershipResponse> changeRole(
            @PathVariable Long organizationId,
            @PathVariable Long userId,
            @Valid @RequestBody RoleChangeRequest request) {

        MembershipResponse response = membershipService.changeRole(
                organizationId, userId, request.role());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long userId) {

        membershipService.removeMember(organizationId, userId);

        return ResponseEntity.noContent().build();
    }
}