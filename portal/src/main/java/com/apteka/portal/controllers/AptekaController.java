package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.dtos.request.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.services.AptekaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/apteka")
@RequiredArgsConstructor
public class AptekaController {
    private final AptekaService aptekaService;

    @GetMapping
    public ResponseEntity<List<AptekaResponseDTO>> getAll() {
        return ResponseEntity.ok(aptekaService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(aptekaService.getOne(id));
    }

    @GetMapping("/me")
    public ResponseEntity<AptekaResponseDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(currentUser.getAptekaId()));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<AptekaResponseDTO>> filter(@RequestBody AptekaFilterRequestDTO dto, Pageable pageable) {
        return ResponseEntity.ok().body(aptekaService.filter(dto, pageable));
    }

    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@Valid @RequestBody AptekaRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaService.create(dto, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody AptekaRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.update(id, dto, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return ResponseEntity.noContent().build();
    }
}
