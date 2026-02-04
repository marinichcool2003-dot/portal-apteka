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
import com.apteka.portal.models.GroupClient;
import com.apteka.portal.services.GroupClientService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-client")
@RequiredArgsConstructor
public class GroupClientController {
    private final GroupClientService groupClientService;

    @GetMapping
    public ResponseEntity<List<GroupClient>> getAll(){
        return ResponseEntity
            .ok(groupClientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupClient> getOne(@PathVariable Integer id){
        return ResponseEntity
            .ok(groupClientService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<GroupClient> create(@RequestBody GroupClientRequestDTO groupClientRequestDTO){
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(groupClientService.create(groupClientRequestDTO.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupClient> update(@PathVariable Integer id, @RequestBody GroupClientRequestDTO groupClientRequestDTO){
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
