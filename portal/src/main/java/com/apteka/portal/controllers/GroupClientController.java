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

import com.apteka.portal.dtos.request.GroupClientRequestDTO;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.services.UserGroupService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-client")
@RequiredArgsConstructor
public class GroupClientController {
    private final UserGroupService groupClientService;

    @GetMapping
    public ResponseEntity<List<UserGroup>> getAll(){
        return ResponseEntity
            .ok(groupClientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserGroup> getOne(@PathVariable Integer id){
        return ResponseEntity
            .ok(groupClientService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<UserGroup> create(@RequestBody GroupClientRequestDTO groupClientRequestDTO){
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(groupClientService.create(groupClientRequestDTO.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserGroup> update(@PathVariable Integer id, @RequestBody GroupClientRequestDTO groupClientRequestDTO){
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(groupClientService.update(id, groupClientRequestDTO.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id){
        groupClientService.delete(id);
        return ResponseEntity
            .noContent()
            .build();
    }
}
