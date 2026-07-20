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

import com.apteka.portal.dtos.request.mainpagelinks.GroupMainPageLinkUpdateRequestDTO;
import com.apteka.portal.dtos.request.mainpagelinks.GroupMainPageLinksRequestDTO;
import com.apteka.portal.dtos.response.mainpagelink.GroupMainPageLinksResponseDTO;
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
    @PreAuthorize("@security.hasAnyAction('CAN_SELECT_NON_ACTIVE_MAIN_PAGE_LINK', 'CREATE_MAIN_PAGE_LINK', 'UPDATE_MAIN_PAGE_LINK', 'SAFE_DELETE_MAIN_PAGE_LINK', 'CAN_ACTIVATE_MAIN_PAGE_LINK_AFTER_SAFE_DELETE', 'PERMANENT_DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    @GetMapping("/get-all")
    public ResponseEntity<List<GroupMainPageLinksResponseDTO>> getAll(
            @RequestParam(defaultValue = "true") Boolean isActive,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(groupMainPageLinksService.getAll(isActive, currentUser));
    }

    @Operation(summary = "Создать группу ссылок")
    @PreAuthorize("@security.hasAction('CREATE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<GroupMainPageLinksResponseDTO> create(@Valid @RequestBody GroupMainPageLinksRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupMainPageLinksService.create(dto));
    }

    @Operation(summary = "Изменить группу ссылок")
    @PreAuthorize("@security.hasAction('UPDATE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<GroupMainPageLinksResponseDTO> update(@PathVariable Integer id,
            @Valid @RequestBody GroupMainPageLinkUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(groupMainPageLinksService.update(id, dto, currentUser));
    }

    @Operation(summary = "Безопасно удалить группу ссылок")
    @PatchMapping("/safe-delete/{id}")
    @PreAuthorize("@security.hasAction('SAFE_DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<Void> safeDelete(@PathVariable Integer id) {
        groupMainPageLinksService.safeDelete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Восстановить группу ссылок")
    @PatchMapping("/restore/{id}")
    @PreAuthorize("@security.hasAction('SAFE_DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        groupMainPageLinksService.restore(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить группу ссылок")
    @PreAuthorize("@security.hasAction('DELETE_MAIN_PAGE_LINK') or @security.hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> permanentDelete(@PathVariable Integer id) {
        groupMainPageLinksService.permanentDelete(id);
        return ResponseEntity.noContent().build();
    }
}
