package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.UserGroupRequestDTO;
import com.apteka.portal.dtos.response.UserGroupResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.UserGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/user-groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserGroupService userGroupService;

    @GetMapping
    public ResponseEntity<List<UserGroupResponseDTO>> getAll() {
        return ResponseEntity.ok(userGroupService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGroupResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(userGroupService.getOne(id));
    }

    @PreAuthorize("@appSecurity.isClient() and hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserGroupResponseDTO> create(@Valid @RequestBody UserGroupRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userGroupService.create(dto, currentUser));
    }

    @PreAuthorize("@appSecurity.isClient() and hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserGroupResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody UserGroupRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(userGroupService.update(id, dto, currentUser));
    }

    @PreAuthorize("@appSecurity.isClient() and hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        userGroupService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
