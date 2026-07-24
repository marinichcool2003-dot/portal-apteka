package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.DepartmentFilterRequestDTO;
import com.apteka.portal.dtos.request.DepartmentFullFilterRequestDTO;
import com.apteka.portal.dtos.request.task.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.task.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.TaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PreAuthorize("@security.hasAction('CAN_SELECT_ANOTHER_GROUP_TASKS') or @security.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<TaskShortResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getOne(id, currentUser));
    }

    @GetMapping("/tasks-assigned-me")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksAssignedMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getMyDepartmentTasks(filter, currentUser, pageable));
    }

    @GetMapping("/group-user-stats")
    public ResponseEntity<List<DepartmentTaskStatsDTO>> getGroupsUserStats(
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getGroupsUserStats(currentUser));
    }
    
    @GetMapping("/group-user-stats/{id}")
    public ResponseEntity<DepartmentTaskStatsDTO> getGroupUserStats(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getGroupUserStats(id, currentUser));
    }

    @GetMapping("/tasks-assigned-my-group")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksAssignedMyGroup(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .groupId(currentUser.getUserGroup().getId())
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter, pageable));
    }

    @GetMapping("/tasks-created-by-me")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksCreatedByMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();
        return ResponseEntity.ok(taskService.getCreatedMeTasks(filter, currentUser, pageable));
    }

    @PreAuthorize("@security.hasAction('CAN_SELECT_ANOTHER_GROUP_TASKS') or @security.hasRole('ADMIN')")
    @GetMapping("/full-filter")
    public ResponseEntity<Page<TaskShortResponseDTO>> getDepartamentTaskWithFilters(
            @Valid @ModelAttribute DepartmentFullFilterRequestDTO dto, Pageable pageable) {
        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .groupId(dto.groupId())
                .creatorId(dto.creatorId())
                .assignerId(dto.assignerId())
                .build();
        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter, pageable));
    }

    @PostMapping
    public ResponseEntity<TaskShortResponseDTO> create(@Valid @RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskShortResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.update(id, dto, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
