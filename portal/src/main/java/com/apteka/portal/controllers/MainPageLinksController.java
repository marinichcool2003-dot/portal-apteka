package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.MainPageLinkRequestDTO;
import com.apteka.portal.dtos.request.MainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.response.MainPageLinkResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.MainPageLinksService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/main-page-links")
@RequiredArgsConstructor
@Tag(name = "Управление ссылками на главной странице")
public class MainPageLinksController {
    private final MainPageLinksService mainPageLinksService;

    @Operation(summary = "Получить ссылки по группе")
    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<List<MainPageLinkResponseDTO>> getByGroup(@PathVariable Integer groupId) {
        return ResponseEntity.ok(mainPageLinksService.getByGroup(groupId));
    }

    @Operation(summary = "Получить ссылку по id")
    @GetMapping("/{id}")
    public ResponseEntity<MainPageLinkResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(mainPageLinksService.getOne(id));
    }

    @Operation(summary = "Получить все ссылки")
    @GetMapping("/get-all")
    public ResponseEntity<List<MainPageLinkResponseDTO>> getAll() {
        return ResponseEntity.ok(mainPageLinksService.getAll());
    }

    @Operation(summary = "Создать ссылку")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LINK_CHANGER')")
    public ResponseEntity<MainPageLinkResponseDTO> create(@Valid @RequestBody MainPageLinkRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mainPageLinksService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить ссылку")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LINK_CHANGER')")
    public ResponseEntity<MainPageLinkResponseDTO> update(@PathVariable Integer id,
            @Valid @RequestBody MainPageLinkUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(mainPageLinksService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить ссылку")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        mainPageLinksService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
