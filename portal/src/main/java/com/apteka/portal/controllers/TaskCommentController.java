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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.TaskCommentRequestDTO;
import com.apteka.portal.dtos.response.TaskCommentResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.TaskCommentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/task-comments")
@RequiredArgsConstructor
@Tag(name = "Комментарии к задачам")
public class TaskCommentController {
    private final TaskCommentService taskCommentService;

    @Operation(summary = "Получение всех комментариев к задаче")
    @GetMapping("/by-task/{taskId}")
    public ResponseEntity<List<TaskCommentResponseDTO>> getByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskCommentService.getByTask(taskId));
    }

    @Operation(summary = "Получение комментария по ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskCommentResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskCommentService.getOne(id));
    }

    @Operation(summary = "Создание комментария")
    @PostMapping
    public ResponseEntity<TaskCommentResponseDTO> create(@Valid @RequestBody TaskCommentRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskCommentService.create(dto, currentUser));
    }

    @Operation(summary = "Удаление комментария (Только ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskCommentService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
