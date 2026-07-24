package com.apteka.portal.controllers;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaUpdateDescriptionRequestDTO;
import com.apteka.portal.dtos.response.apteka.AptekaResponseDTO;
import com.apteka.portal.services.AptekaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/apteka")
@RequiredArgsConstructor
public class AptekaController {
    private final AptekaService aptekaService;

    @GetMapping
    public ResponseEntity<Page<AptekaResponseDTO>> getAll(Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getAll(pageable, isActive, currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable UUID id,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(id, isActive, currentUser));
    }

    @GetMapping("/me")
    public ResponseEntity<AptekaResponseDTO> getMe(@RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(currentUser.getInternalId(), isActive, currentUser));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<AptekaResponseDTO>> filter(@Valid @ModelAttribute AptekaFilterRequestDTO dto,
            Pageable pageable, @RequestParam(defaultValue = "true") Boolean isActive, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok().body(aptekaService.filter(dto, pageable, isActive, currentUser));
    }

    @PreAuthorize("@security.hasAction('CREATE_APTEKA') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@Valid @RequestBody AptekaRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaService.create(dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasAction('UPDATE_APTEKA_ACCOUNT') or @security.hasRole('ADMIN')")
    @PutMapping("/account/{id}")
    public ResponseEntity<AptekaResponseDTO> updateAccount(@PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateAccount(id, dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasAction('UPDATE_APTEKA_DESCRIPTION') or @security.hasRole('ADMIN')")
    @PutMapping("/description/{id}")
    public ResponseEntity<AptekaResponseDTO> updateDescription(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateDescriptionRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateDescription(id, dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.update(id, dto, currentUser));
    }

    @PreAuthorize("@security.hasAction('SAFE_DELETE_APTEKA') or @security.hasAction('PERMANENT_DELETE_APTEKA') or @security.hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("@security.hasAction('PERMANENT_DELETE_APTEKA') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.permanentDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}