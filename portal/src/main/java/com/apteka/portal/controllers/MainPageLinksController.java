package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.mainpagelinks.MainPageLinkRequestDTO;
import com.apteka.portal.dtos.request.mainpagelinks.MainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.response.mainpagelink.MainPageLinkResponseDTO;
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
    @PreAuthorize("""
        @security.hasAction('CAN_SELECT_NON_ACTIVE_MAIN_PAGE_LINK') 
        or @security.hasAction('CREATE_MAIN_PAGE_LINK') 
        or @security.hasAction('UPDATE_MAIN_PAGE_LINK') 
        or @security.hasAction('SAFE_DELETE_MAIN_PAGE_LINK') 
        or @security.hasAction('CAN_ACTIVATE_MAIN_PAGE_LINK_AFTER_SAFE_DELETE') 
        or @security.hasAction('PERMANENT_DELETE_MAIN_PAGE_LINK') 
        or @security.hasRole('ADMIN')
            """)
    @GetMapping("/by-group/{groupId}")
    public ResponseEntity<List<MainPageLinkResponseDTO>> getByGroup(@PathVariable Integer groupId,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(mainPageLinksService.getByGroup(groupId, isActive, currentUser));
    }

    @Operation(summary = "Получить все ссылки (Вывод на главной странице)")
    @GetMapping
    public ResponseEntity<List<MainPageLinkResponseDTO>> getAll() {
        return ResponseEntity.ok(mainPageLinksService.getAll());
    }

    @Operation(summary = "Создать ссылку")
    @PostMapping
    @PreAuthorize("@security.hasAction('CREATE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<MainPageLinkResponseDTO> create(@Valid @RequestBody MainPageLinkRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mainPageLinksService.create(dto));
    }

    @Operation(summary = "Обновить ссылку")
    @PutMapping("/{id}")
    @PreAuthorize("@security.hasAction('UPDATE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<MainPageLinkResponseDTO> update(@PathVariable Integer id,
            @Valid @RequestBody MainPageLinkUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(mainPageLinksService.update(id, dto, currentUser));
    }

    @Operation(summary = "Безопасно удалить ссылку")
    @PatchMapping("/safe-delete/{id}")
    @PreAuthorize("@security.hasAction('SAFE_DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id) {
        mainPageLinksService.safeDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Восстановить ссылку")
    @PatchMapping("/restore/{id}")
    @PreAuthorize("@security.hasAction('SAFE_DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        mainPageLinksService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить ссылку")
    @DeleteMapping("/{id}")
    @PreAuthorize("@security.hasAction('DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        mainPageLinksService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
