package com.apteka.portal.controllers;

import com.apteka.portal.dtos.request.usergroup.UserGroupUpdateRequestDTO;
import com.apteka.portal.dtos.response.usergroup.UserGroupResponseDTO;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.usergroup.UserGroupRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.UserGroupService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/user-groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserGroupService userGroupService;

    @PreAuthorize("@security.hasAction('CAN_SELECT_ALL_ACTIVE_GROUPS') or @security.hasAction('CAN_SELECT_ALL_NON_ACTIVE_GROUPS') or @security.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserGroupResponseDTO>> getAll(@AuthenticationPrincipal AppUserDetails currentUser, @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(userGroupService.findByActive(currentUser, isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGroupResponseDTO> getOne(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser, @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(userGroupService.getOne(id, currentUser, isActive));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGroupResponseDTO> getOneWithVisibleGroups(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser, @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok();
    }

    @GetMapping("/visible")
    public ResponseEntity<List<UserGroupResponseDTO>> getWithVisible(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(userGroupService.getWithVisible(currentUser));
    }

    @PreAuthorize("@security.hasAction('CAN_CREATE_USER_GROUP') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserGroupResponseDTO> create(@Valid @RequestBody UserGroupRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userGroupService.create(dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('CAN_UPDATE_SELF_USER_GROUP') or @security.hasAction('CAN_UPDATE_USER_GROUP') or @security.hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserGroupResponseDTO> update(@PathVariable Integer id,
            @Valid @RequestBody UserGroupUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser)
            throws IOException {
        return ResponseEntity.ok(userGroupService.update(id, dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('SAFE_DELETE_USER_GROUP') or @security.hasRole('ADMIN')")
    @PatchMapping("/safe-delete/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        userGroupService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@security.hasAction('CAN_ACTIVATE_USER_GROUP_AFTER_SAFE_DELETE') or @security.hasRole('ADMIN')")
    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restoreAfterSafeDelete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        userGroupService.restoreAfterSafeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@security.hasAction('PERMANENT_DELETE_USER_GROUP') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm) {
        userGroupService.permanentDelete(id, currentUser, confirm);
        return ResponseEntity.noContent().build();
    }
}
