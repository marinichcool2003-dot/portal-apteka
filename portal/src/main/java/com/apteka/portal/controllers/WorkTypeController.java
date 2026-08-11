package com.apteka.portal.controllers;

import com.apteka.portal.services.WorkTypeService;

import jakarta.validation.Valid;

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

import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.request.WorkTypeUpdateRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.models.AppUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Виды работ", description = "Управление видами работ в категориях задач")
@RestController
@RequestMapping("/api/v1/work-types")
@RequiredArgsConstructor
public class WorkTypeController {
    private final WorkTypeService workTypeService;

    @Operation(summary = "Виды работ по категории задач", description = "Возвращает виды работ указанной категории задач (group-task) с фильтром по активности.")
    @GetMapping("/by-group-task/{groupTaskId}")
    public ResponseEntity<List<WorkTypeResponseDTO>> getByGroupTask(@PathVariable Integer groupTaskId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(workTypeService.getByGroupTask(groupTaskId, currentUser, isActive));
    }

    @Operation(summary = "Получить вид работ по ID", description = "Возвращает один вид работ по идентификатору.")
    @GetMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> getOne(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(workTypeService.getOne(id, currentUser));
    }

    @Operation(summary = "Создать вид работ", description = "Создаёт новый вид работ. syncToAllIntended — на все sibling APTEKA GroupTask.")
    @PreAuthorize("@security.hasAction('BASE_WORK_WITH_WORK_TYPE') or @security.hasAction('GRAND_WORK_WITH_WORK_TYPE') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<WorkTypeResponseDTO> create(@Valid @RequestBody WorkTypeRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workTypeService.create(dto, currentUser, syncToAllIntended));
    }

    @Operation(summary = "Обновить вид работ", description = "Обновляет вид работ. confirm + syncToAllIntended.")
    @PreAuthorize("""
            @security.hasAction('BASE_WORK_WITH_WORK_TYPE')
            or @security.hasAction('GRAND_WORK_WITH_WORK_TYPE')
            or @security.hasAction('NON_SAFE_UPDATE_WORK_TYPE')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> update(@PathVariable Integer id,
            @Valid @RequestBody WorkTypeUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        return ResponseEntity.ok().body(workTypeService.update(id, dto, currentUser, confirm, syncToAllIntended));
    }

    @Operation(summary = "Мягкое удаление вида работ", description = "Деактивирует вид работ. syncToAllIntended — sibling APTEKA.")
    @PreAuthorize("@security.hasAction('BASE_WORK_WITH_WORK_TYPE') or @security.hasAction('GRAND_WORK_WITH_WORK_TYPE') or @security.hasRole('ADMIN')")
    @PatchMapping("/safe-delete/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        workTypeService.safeDelete(id, currentUser, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Восстановить вид работ", description = "Восстанавливает вид работ. syncToAllIntended — sibling APTEKA.")
    @PreAuthorize("@security.hasAction('BASE_WORK_WITH_WORK_TYPE') or @security.hasAction('GRAND_WORK_WITH_WORK_TYPE') or @security.hasRole('ADMIN')")
    @PatchMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        workTypeService.restore(id, currentUser, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Полное удаление вида работ", description = "Безвозвратно удаляет вид работ. confirm + syncToAllIntended.")
    @PreAuthorize("@security.hasAction('CAN_PERMANENT_DELETE_WORK_TYPE') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "false") Boolean confirm,
            @RequestParam(defaultValue = "false") Boolean syncToAllIntended) {
        workTypeService.permanentDelete(id, currentUser, confirm, syncToAllIntended);
        return ResponseEntity.noContent().build();
    }
}
