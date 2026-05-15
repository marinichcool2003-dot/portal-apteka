package com.apteka.portal.controllers;

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

import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.services.GroupTaskService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-tasks")
@RequiredArgsConstructor
public class GroupTaskController {
    private final GroupTaskService groupTaskService;

    @GetMapping("/by-user-group/{userGroupId}")
    public ResponseEntity<List<GroupTaskResponseDTO>> getByUserGroup(@PathVariable Integer userGroupId) {
        return ResponseEntity.ok(groupTaskService.getByUserGroup(userGroupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupTaskResponseDTO> getOne(Integer id) {
        return ResponseEntity.ok(groupTaskService.getOne(id));
    }

    @PreAuthorize("@appSecurity.isClient() and hasAnyRole('ADMIN', 'BOSS')")
    @PostMapping
    public ResponseEntity<GroupTaskResponseDTO> create(@Valid @RequestBody GroupTaskRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(groupTaskService.create(dto));
    }

    @PreAuthorize("@appSecurity.isClient() and hasAnyRole('ADMIN', 'BOSS')")
    @PutMapping
    public ResponseEntity<GroupTaskResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody GroupTaskRequestDTO dto) {
        return ResponseEntity.ok(groupTaskService.update(id, dto));
    }

    @PreAuthorize("@appSecurity.isClient() and hasAnyRole('ADMIN', 'BOSS')")
    @DeleteMapping("/{id}") 
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        groupTaskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
