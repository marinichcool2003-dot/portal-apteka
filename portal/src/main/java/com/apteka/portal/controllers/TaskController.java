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
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getOne(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/full-filter")
    public ResponseEntity<List<TaskResponseDTO>> getDepartamentTaskWithFilters(
            @RequestBody DepartamentTaskWithFiltersDTO dto) {
        return ResponseEntity.ok(taskService.getDepartamentTaskWithFilters(dto));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@RequestBody TaskCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(dto, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable Long id, @RequestBody TaskUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(taskService.update(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal AppUserDetails currentUser) {
        taskService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
