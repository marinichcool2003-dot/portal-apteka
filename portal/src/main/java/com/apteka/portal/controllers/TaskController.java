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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.services.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @GetMapping
    public ResponseEntity<List<TaskShortResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getOne(id));
    }

    @GetMapping("/tasks-assigned-me")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksAssignedMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Integer workTypeId,
            @RequestParam(required = false) Integer groupTaskId) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .groupId(currentUser.getUserGroup().getId())
                .status(status)
                .priority(priority)
                .workTypeId(workTypeId)
                .groupTaskId(groupTaskId)
                .build();

        return ResponseEntity.ok(taskService.getMyDepartmentTasks(filter, currentUser));
    }

    @GetMapping("/tasks-assigned-my-group")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksAssignedMyGroup(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Integer workTypeId,
            @RequestParam(required = false) Integer groupTaskId) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .groupId(currentUser.getUserGroup().getId())
                .status(status)
                .priority(priority)
                .workTypeId(workTypeId)
                .groupTaskId(groupTaskId)
                .build();

        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter));
    }

    @GetMapping("/tasks-created-by-me")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksCreatedByMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) Integer workTypeId,
            @RequestParam(required = false) Integer groupTaskId) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(status)
                .priority(priority)
                .workTypeId(workTypeId)
                .groupTaskId(groupTaskId)
                .build();

        return ResponseEntity.ok(taskService.getCreatedMeTasks(filter, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/full-filter")
    public ResponseEntity<List<TaskShortResponseDTO>> getDepartamentTaskWithFilters(
            @RequestBody DepartamentTaskWithFiltersDTO dto) {
        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(dto));
    }

    @PostMapping
    public ResponseEntity<TaskShortResponseDTO> create(@RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskShortResponseDTO> update(@PathVariable Long id, @RequestBody TaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.update(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
