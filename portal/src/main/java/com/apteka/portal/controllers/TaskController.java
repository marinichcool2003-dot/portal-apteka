package com.apteka.portal.controllers;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Задачи")
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Получить весь список задач (Только ADMIN, BOSS)")
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @GetMapping
    public ResponseEntity<Page<TaskShortResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAll(pageable));
    }

    @Operation(summary = "Получить задачу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getOne(id, currentUser));
    }

    @Operation(summary = "Получить задачи назначенные авторизированному пользователю")
    @GetMapping("/tasks-assigned-me")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksAssignedMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getMyDepartmentTasks(filter, currentUser, pageable));
    }

    @Operation(summary = "Получить статистику всех групп по задачам")
    @GetMapping("/group-user-stats")
    public ResponseEntity<List<DepartmentTaskStatsDTO>> getGroupsUserStats() {
        return ResponseEntity.ok(taskService.getGroupsUserStats());
    }
    
    @Operation(summary = "Получить статистику группы по задачам")
    @GetMapping("/group-user-stats/{id}")
    public ResponseEntity<DepartmentTaskStatsDTO> getGroupUserStats(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getGroupUserStats(id));
    }

    @Operation(summary = "Получить задачи назначенные на группу данного пользователя")
    @GetMapping("/tasks-assigned-my-group")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksAssignedMyGroup(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .groupId(currentUser.getUserGroup().getId())
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter, pageable));
    }

    @Operation(summary = "Получить задачи созданные авторизированным пользователем")
    @GetMapping("/tasks-created-by-me")
    public ResponseEntity<Page<TaskShortResponseDTO>> getTasksCreatedByMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto, Pageable pageable) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();
        System.out.println(dto);

        return ResponseEntity.ok(taskService.getCreatedMeTasks(filter, currentUser, pageable));
    }

    @Operation(summary = "Получить задачи по всем возможным фильтрам (Только для ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/full-filter")
    public ResponseEntity<Page<TaskShortResponseDTO>> getDepartamentTaskWithFilters(
            @ParameterObject @Valid @ModelAttribute DepartmentFullFilterRequestDTO dto, Pageable pageable) {
        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .groupId(dto.groupId())
                .creatorId(dto.creatorId())
                .assignerId(dto.assignerId())
                .build();
        System.out.println(dto);
        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter, pageable));
    }

    @Operation(summary = "Создать задачу")
    @PostMapping
    public ResponseEntity<TaskShortResponseDTO> create(@Valid @RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @Operation(summary = "Изменить задачу")
    @PutMapping("/{id}")
    public ResponseEntity<TaskShortResponseDTO> update(@Valid @PathVariable Long id, @RequestBody TaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить задачу (Только для ADMIN")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
