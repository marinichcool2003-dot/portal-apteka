package com.apteka.portal.controllers;

import com.apteka.portal.repository.WorkTypeRepository;
import com.apteka.portal.services.WorkTypeService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/work-types")
@RequiredArgsConstructor
@PreAuthorize("@appSecurity.isClient()")
public class WorkTypeController {
    private final WorkTypeService workTypeService;

    @GetMapping("/by-group-task/{groupTaskId}")
    public ResponseEntity<List<WorkTypeResponseDTO>> getByGroupTask(@PathVariable Integer groupTaskId) {
        return ResponseEntity.ok(workTypeService.getByGroupTask(groupTaskId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(workTypeService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<WorkTypeResponseDTO> create(@Valid @RequestBody WorkTypeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(workTypeService.create(dto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody WorkTypeRequestDTO dto) {
        return ResponseEntity.ok().body(workTypeService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        workTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
