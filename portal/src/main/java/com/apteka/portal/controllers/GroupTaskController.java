package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.services.GroupTaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-task")
@RequiredArgsConstructor
public class GroupTaskController {
    private final GroupTaskService groupTaskService;

    @GetMapping
    public ResponseEntity<List<GroupTask>> getAll() {
        return ResponseEntity.ok(groupTaskService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupTask> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(groupTaskService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<GroupTask> create(@RequestBody GroupTaskRequestDTO groupTaskRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupTaskService.create(groupTaskRequestDTO.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupTask> update(@PathVariable Integer id,
            @RequestBody GroupTaskRequestDTO groupTaskRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(groupTaskService.update(id, groupTaskRequestDTO.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        groupTaskService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
