package com.apteka.portal.controllers;

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

import com.apteka.portal.dtos.request.grouptask.GroupTaskRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.GroupTaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-tasks")
@RequiredArgsConstructor
public class GroupTaskController {
    private final GroupTaskService groupTaskService;

    @PreAuthorize("@security.hasAction('GRAND_WORK_WITH_GROUP_TASK') or @security.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<GroupTaskResponseDTO>> getByUserGroups(
            @RequestParam Integer creatorGroupId,
            @RequestParam Integer executorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(creatorGroupId, executorGroupId, isActive, currentUser));
    }

    @GetMapping("/to/{executorGroupId}")
    public ResponseEntity<List<GroupTaskResponseDTO>> getExecutorGroupTasksFromMyGroup(
            @PathVariable Integer executorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(currentUser.getUserGroup().getId(), executorGroupId,
                isActive, currentUser));
    }

    @GetMapping("/from/{creatorGroupId}")
    public ResponseEntity<List<GroupTaskResponseDTO>> getCreatorGroupTasksFromMyGroup(
            @PathVariable Integer creatorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(creatorGroupId, currentUser.getUserGroup().getId(),
                isActive, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupTaskResponseDTO> getOne(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(groupTaskService.getOne(id, currentUser));
    }

    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
            or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
            or @security.hasRole('ADMIN')
            """)
    @PostMapping
    public ResponseEntity<GroupTaskResponseDTO> create(@Valid @RequestBody GroupTaskRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupTaskService.create(dto, currentUser));
    }

    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
            or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
            or @security.hasAction('NON_SAFE_UPDATE_GROUP_TASK')
            or @security.hasRole('ADMIN')
            """)
    @PutMapping("/{id}")
    public ResponseEntity<GroupTaskResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody GroupTaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm) {
        return ResponseEntity.ok(groupTaskService.update(id, dto, currentUser, confirm));
    }

    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
            or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
            or @security.hasRole('ADMIN')
            """)
    @PatchMapping("/safe-delete/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        groupTaskService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("""
        @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
        or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
        or @security.hasRole('ADMIN')
        """)
    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        groupTaskService.restore(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@security.hasAction('CAN_PERMANENT_DELETE_GROUP_TASK') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm) {
        groupTaskService.permanentDelete(id, currentUser, confirm);
        return ResponseEntity.noContent().build();
    }
}
