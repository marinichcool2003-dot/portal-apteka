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

import com.apteka.portal.dtos.request.GroupAptekiRequestDTO;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.services.GroupAptekiService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/group-apteki")
@RequiredArgsConstructor
public class GroupAptekiController {
    private final GroupAptekiService groupAptekiService;

    @GetMapping
    public ResponseEntity<List<GroupApteki>> getAll() {
        return ResponseEntity
                .ok(groupAptekiService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupApteki> getOne(@PathVariable Integer id) {
        return ResponseEntity
                .ok(groupAptekiService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<GroupApteki> create(@RequestBody GroupAptekiRequestDTO groupAptekiRequestDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(groupAptekiService.create(groupAptekiRequestDTO.name()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupApteki> update(@PathVariable Integer id, @RequestBody GroupAptekiRequestDTO groupAptekiRequestDTO){
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(groupAptekiService.update(id, groupAptekiRequestDTO.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        groupAptekiService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
