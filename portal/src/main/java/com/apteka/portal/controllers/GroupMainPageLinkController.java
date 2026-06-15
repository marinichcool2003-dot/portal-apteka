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

import com.apteka.portal.dtos.request.GroupMainPageLinksRequestDTO;
import com.apteka.portal.dtos.response.GroupMainPageLinksResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.GroupMainPageLinksService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/groups-main-page-links")
@RequiredArgsConstructor
@Tag(name = "Работа с группами ссылок на главной странице")
public class GroupMainPageLinkController {
    private final GroupMainPageLinksService groupMainPageLinksService;

    @Operation(summary = "Получить все группы ссылок")
    @GetMapping("/get-all")
    public ResponseEntity<List<GroupMainPageLinksResponseDTO>> getAll() {
        return ResponseEntity.ok(groupMainPageLinksService.getAll());
    }

    @Operation(summary = "Получить группу ссылок по ID")
    @GetMapping("/{id}")
    public ResponseEntity<GroupMainPageLinksResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(groupMainPageLinksService.getOne(id));
    }

    @Operation(summary = "Создать группу ссылок")
    @PreAuthorize("hasAnyRole('ADMIN', 'LINK_CHANGER')")
    @PostMapping
    public ResponseEntity<GroupMainPageLinksResponseDTO> create(@Valid @RequestBody GroupMainPageLinksRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMainPageLinksService.create(dto, currentUser));
    }

    @Operation(summary = "Изменить группу ссылок")
    @PreAuthorize("hasAnyRole('ADMIN', 'LINK_CHANGER')")
    @PutMapping("/{id}")
    public ResponseEntity<GroupMainPageLinksResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody GroupMainPageLinksRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(groupMainPageLinksService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить группу ссылок")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        groupMainPageLinksService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
