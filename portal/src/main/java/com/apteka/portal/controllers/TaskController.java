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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Задачи", description = "Управление задачами: списки, фильтры, статистика, создание и обновление")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @Operation(summary = "Список всех задач", description = "Возвращает постраничный список всех задач. Требуется право CAN_SELECT_ANOTHER_GROUP_TASKS или роль ADMIN.")
    @PreAuthorize("@security.hasAction('CAN_SELECT_ANOTHER_GROUP_TASKS') or @security.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<TaskShortResponseDTO>> getAll(Pageable pageable) {
        return ResponseEntity.ok(taskService.getAll(pageable));
    }

    @Operation(summary = "Получить задачу по ID", description = "Возвращает полные данные одной задачи.")
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getOne(id, currentUser));
    }

    @Operation(summary = "Задачи, назначенные мне", description = "Возвращает задачи текущего пользователя как исполнителя с фильтрами по статусу, приоритету, виду работ и категории.")
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

    @Operation(summary = "Статистика задач по группам", description = "Возвращает статистику задач по группам, видимым текущему пользователю.")
    @GetMapping("/group-user-stats")
    public ResponseEntity<List<DepartmentTaskStatsDTO>> getGroupsUserStats(
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getGroupsUserStats(currentUser));
    }
    
    @Operation(summary = "Статистика задач группы", description = "Возвращает статистику задач указанной группы пользователей.")
    @GetMapping("/group-user-stats/{id}")
    public ResponseEntity<DepartmentTaskStatsDTO> getGroupUserStats(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.getGroupUserStats(id, currentUser));
    }

    @Operation(summary = "Задачи моей группы", description = "Возвращает задачи, назначенные группе текущего пользователя, с фильтрами.")
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

    @Operation(summary = "Задачи, созданные мной", description = "Возвращает задачи, созданные текущим пользователем, с фильтрами.")
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

    @Operation(summary = "Расширенный фильтр задач", description = "Полная фильтрация задач по статусу, приоритету, группе, создателю, исполнителю и др.")
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

    @Operation(summary = "Создать задачу", description = "Создаёт новую задачу.")
    @PostMapping
    public ResponseEntity<TaskShortResponseDTO> create(@Valid @RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить задачу", description = "Обновляет существующую задачу.")
    @PutMapping("/{id}")
    public ResponseEntity<TaskShortResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить задачу", description = "Удаляет задачу. Доступно только администратору.")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
