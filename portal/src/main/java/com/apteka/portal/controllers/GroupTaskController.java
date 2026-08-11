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

import com.apteka.portal.dtos.request.grouptask.GroupTaskBulkToAptekaRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskBulkToAptekaResponseDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.GroupTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Категории задач (group-tasks)", description = "Управление категориями задач между группами: создание, обновление, удаление")
@RestController
@RequestMapping("/api/v1/group-tasks")
@RequiredArgsConstructor
public class GroupTaskController {
    private final GroupTaskService groupTaskService;

    @Operation(summary = "Категории задач между группами", description = "Возвращает категории задач по группе-создателю и группе-исполнителю. Требуется GRAND_WORK_WITH_GROUP_TASK или ADMIN.")
    @PreAuthorize("@security.hasAction('GRAND_WORK_WITH_GROUP_TASK') or @security.hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<GroupTaskResponseDTO>> getByUserGroups(
            @RequestParam Integer creatorGroupId,
            @RequestParam Integer executorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(creatorGroupId, executorGroupId, isActive, currentUser));
    }

    @Operation(summary = "Категории задач к группе-исполнителю", description = "Возвращает категории задач от группы текущего пользователя к указанной группе-исполнителю.")
    @GetMapping("/to/{executorGroupId}")
    public ResponseEntity<List<GroupTaskResponseDTO>> getExecutorGroupTasksFromMyGroup(
            @PathVariable Integer executorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(currentUser.getUserGroup().getId(), executorGroupId,
                isActive, currentUser));
    }

    @Operation(summary = "Категории задач от группы-создателя", description = "Возвращает категории задач от указанной группы-создателя к группе текущего пользователя.")
    @GetMapping("/from/{creatorGroupId}")
    public ResponseEntity<List<GroupTaskResponseDTO>> getCreatorGroupTasksFromMyGroup(
            @PathVariable Integer creatorGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(groupTaskService.getByGroups(creatorGroupId, currentUser.getUserGroup().getId(),
                isActive, currentUser));
    }

    @Operation(summary = "Получить категорию задач по ID", description = "Возвращает одну категорию задач по идентификатору.")
    @GetMapping("/{id}")
    public ResponseEntity<GroupTaskResponseDTO> getOne(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(groupTaskService.getOne(id, currentUser));
    }

    @Operation(summary = "Создать категорию задач", description = "Создаёт новую категорию задач между группами.")
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

    @Operation(summary = "Массовое создание категории на все группы аптек", description = "Создаёт категорию задач для каждой видимой APTEKA_GROUP отдела (visibleGroups). Опционально создаёт workTypes на каждый GroupTask.")
    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
            or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
            or @security.hasRole('ADMIN')
            """)
    @PostMapping("/bulk-to-apteka")
    public ResponseEntity<GroupTaskBulkToAptekaResponseDTO> createBulkToApteka(
            @Valid @RequestBody GroupTaskBulkToAptekaRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupTaskService.createBulkToApteka(dto, currentUser));
    }

    @Operation(summary = "Обновить категорию задач", description = "Обновляет категорию задач. confirm — подтверждение; syncToAllIntended — применить к sibling APTEKA intended.")
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
            @RequestParam(defaultValue = "false") Boolean confirm,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        return ResponseEntity.ok(groupTaskService.update(id, dto, currentUser, confirm, syncToAllIntended));
    }

    @Operation(summary = "Мягкое удаление категории задач", description = "Деактивирует категорию задач (safe delete). syncToAllIntended — sibling APTEKA.")
    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
            or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
            or @security.hasRole('ADMIN')
            """)
    @PatchMapping("/safe-delete/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        groupTaskService.safeDelete(id, currentUser, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Восстановить категорию задач", description = "Восстанавливает категорию задач после мягкого удаления. syncToAllIntended — sibling APTEKA.")
    @PreAuthorize("""
        @security.hasAction('BASE_WORK_WITH_GROUP_TASK')
        or @security.hasAction('GRAND_WORK_WITH_GROUP_TASK')
        or @security.hasRole('ADMIN')
        """)
    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        groupTaskService.restore(id, currentUser, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Полное удаление категории задач", description = "Безвозвратно удаляет категорию задач. confirm + syncToAllIntended.")
    @PreAuthorize("@security.hasAction('CAN_PERMANENT_DELETE_GROUP_TASK') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        groupTaskService.permanentDelete(id, currentUser, confirm, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }
}
