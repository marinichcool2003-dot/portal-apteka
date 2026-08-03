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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Аптеки", description = "Управление аптеками: список, фильтр, создание, обновление и удаление")
@RestController
@RequestMapping("/api/v1/apteka")
@RequiredArgsConstructor
public class AptekaController {
    private final AptekaService aptekaService;

    @Operation(summary = "Список аптек", description = "Возвращает постраничный список аптек с фильтром по активности (isActive).")
    @GetMapping
    public ResponseEntity<Page<AptekaResponseDTO>> getAll(Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getAll(pageable, isActive, currentUser));
    }

    @Operation(summary = "Получить аптеку по ID", description = "Возвращает данные одной аптеки по идентификатору.")
    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable UUID id,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(id, isActive, currentUser));
    }

    @Operation(summary = "Текущая аптека", description = "Возвращает данные аптеки текущего авторизованного пользователя.")
    @GetMapping("/me")
    public ResponseEntity<AptekaResponseDTO> getMe(@RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(currentUser.getInternalId(), isActive, currentUser));
    }

    @Operation(summary = "Фильтр аптек", description = "Поиск и фильтрация аптек по параметрам запроса с постраничной выдачей.")
    @GetMapping("/filter")
    public ResponseEntity<Page<AptekaResponseDTO>> filter(@Valid @ModelAttribute AptekaFilterRequestDTO dto,
            Pageable pageable, @RequestParam(defaultValue = "true") Boolean isActive, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok().body(aptekaService.filter(dto, pageable, isActive, currentUser));
    }

    @Operation(summary = "Создать аптеку", description = "Создаёт новую аптеку. Требуется право CREATE_APTEKA или роль ADMIN.")
    @PreAuthorize("@security.hasAction('CREATE_APTEKA') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@Valid @RequestBody AptekaRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить учётную запись аптеки", description = "Обновляет данные аккаунта аптеки (логин/пароль и связанные поля).")
    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasAction('UPDATE_APTEKA_ACCOUNT') or @security.hasRole('ADMIN')")
    @PutMapping("/account/{id}")
    public ResponseEntity<AptekaResponseDTO> updateAccount(@PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateAccount(id, dto, currentUser));
    }

    @Operation(summary = "Обновить описание аптеки", description = "Обновляет описание (профильные текстовые поля) аптеки.")
    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasAction('UPDATE_APTEKA_DESCRIPTION') or @security.hasRole('ADMIN')")
    @PutMapping("/description/{id}")
    public ResponseEntity<AptekaResponseDTO> updateDescription(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateDescriptionRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.updateDescription(id, dto, currentUser));
    }

    @Operation(summary = "Полное обновление аптеки", description = "Обновляет основные данные аптеки целиком.")
    @PreAuthorize("@security.hasAction('UPDATE_ALL_APTEKA') or @security.hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> update(@PathVariable UUID id,
            @Valid @RequestBody AptekaUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.update(id, dto, currentUser));
    }

    @Operation(summary = "Мягкое удаление аптеки", description = "Деактивирует аптеку (safe delete) без физического удаления из БД.")
    @PreAuthorize("@security.hasAction('SAFE_DELETE_APTEKA') or @security.hasAction('PERMANENT_DELETE_APTEKA') or @security.hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Полное удаление аптеки", description = "Безвозвратно удаляет аптеку из системы.")
    @PreAuthorize("@security.hasAction('PERMANENT_DELETE_APTEKA') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.permanentDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
