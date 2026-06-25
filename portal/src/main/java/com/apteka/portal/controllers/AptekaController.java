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

import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.dtos.request.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.request.AptekaUpdateDescriptionRequestDTO;
import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;
import com.apteka.portal.services.AptekaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/apteka")
@RequiredArgsConstructor
@Tag(name = "Аптеки")
public class AptekaController {
    private final AptekaService aptekaService;

    @Operation(summary = "Получить список аптек")
    @GetMapping
    public ResponseEntity<Page<AptekaResponseDTO>> getAll(Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getAll(pageable, isActive, currentUser));
    }

    @Operation(summary = "Получить аптеку по ID")
    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable UUID id,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(id, isActive, currentUser));
    }

    @Operation(summary = "Получить текущую аптеку")
    @GetMapping("/me")
    public ResponseEntity<AptekaResponseDTO> getMe(@RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(currentUser.getInternalId(), isActive, currentUser));
    }

    @Operation(summary = "Фильтрация аптек")
    @GetMapping("/filter")
    public ResponseEntity<Page<AptekaResponseDTO>> filter(@Parameter @Valid @ModelAttribute AptekaFilterRequestDTO dto,
            Pageable pageable, @RequestParam(defaultValue = "true") Boolean isActive, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok().body(aptekaService.filter(dto, pageable, isActive, currentUser));
    }

    @Operation(summary = "Создать аптеку")
    @PreAuthorize("hasAction('CREATE_APTEKA') or hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@Valid @RequestBody AptekaRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaService.create(dto, currentUser));
    }

    @Operation(summary = "Обновление аккаунта аптеки")
    @PreAuthorize("hasAnyAction('UPDATE_ALL_APTEKA', 'UPDATE_APTEKA_ACCOUNT') or hasRole('ADMIN')")
    @PutMapping("/account/{id}")
    public ResponseEntity<AptekaResponseDTO> updateAccount(@PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateAccount(id, dto, currentUser));
    }

    @Operation(summary = "Обновление описания аптеки")
    @PreAuthorize("hasAnyAction('UPDATE_ALL_APTEKA', 'UPDATE_APTEKA_DESCRIPTION') or hasRole('ADMIN')")
    @PutMapping("/description/{id}")
    public ResponseEntity<AptekaResponseDTO> updateDescription(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateDescriptionRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateDescription(id, dto, currentUser));
    }

    @Operation(summary = "Полностью обновить аптеку")
    @PreAuthorize("hasAction('UPDATE_ALL_APTEKA') or hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.update(id, dto, currentUser));
    }

    @Operation(summary = "Безопасно удалить аптеку")
    @PreAuthorize("hasAnyAction('SAFE_DELETE_APTEKA', 'PERMANENT_DELETE_APTEKA') or hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Полностью удалить аптеку")
    @PreAuthorize("hasAction('PERMANENT_DELETE_APTEKA') or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.permanentDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}