package com.apteka.portal.controllers;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
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
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
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
    public ResponseEntity<List<TaskShortResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    @Operation(summary = "Получить задачу по ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getOne(id));
    }

    @Operation(summary = "Получить задачи назначенные авторизированному пользователю")
    @GetMapping("/tasks-assigned-me")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksAssignedMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getMyDepartmentTasks(filter, currentUser));
    }

    @Operation(summary = "Получить статистику всех групп по задачам")
    @GetMapping("/group-user-stats")
    public ResponseEntity<List<DepartmentTaskStatsDTO>> getGroupsUserStats() {
        return ResponseEntity.ok(taskService.getGroupsUserStats());
    }

    @Operation(summary = "Получить задачи назначенные на группу данного пользователя")
    @GetMapping("/tasks-assigned-my-group")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksAssignedMyGroup(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .groupId(currentUser.getUserGroup().getId())
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();

        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter));
    }

    @Operation(summary = "Получить задачи созданные авторизированным пользователем")
    @GetMapping("/tasks-created-by-me")
    public ResponseEntity<List<TaskShortResponseDTO>> getTasksCreatedByMe(
            @AuthenticationPrincipal AppUserDetails currentUser,
            @ParameterObject @Valid @ModelAttribute DepartmentFilterRequestDTO dto) {

        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .build();
        System.out.println(dto);

        return ResponseEntity.ok(taskService.getCreatedMeTasks(filter, currentUser));
    }

    @Operation(summary = "Получить задачи по всем возможным фильтрам (Только для ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/full-filter")
    public ResponseEntity<List<TaskShortResponseDTO>> getDepartamentTaskWithFilters(
            @ParameterObject @Valid @ModelAttribute DepartmentFullFilterRequestDTO dto) {
        DepartamentTaskWithFiltersDTO filter = DepartamentTaskWithFiltersDTO.builder()
                .status(dto.status())
                .priority(dto.priority())
                .workTypeId(dto.workTypeId())
                .groupTaskId(dto.groupTaskId())
                .groupId(dto.groupId())
                .creatorClientId(dto.creatorClientId())
                .creatorAptekaId(dto.creatorAptekaId())
                .specificClientId(dto.specificClientId())
                .specificAptekaId(dto.specificAptekaId())
                .build();
        System.out.println(dto);
        return ResponseEntity.ok(taskService.getDepartmentTaskWithFilters(filter));
    }

    @Operation(summary = "Создать задачу")
    @PostMapping
    public ResponseEntity<TaskShortResponseDTO> create(@RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @Operation(summary = "Изменить задачу")
    @PutMapping("/{id}")
    public ResponseEntity<TaskShortResponseDTO> update(@PathVariable Long id, @RequestBody TaskUpdateRequestDTO dto,
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
