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

import com.apteka.portal.dtos.request.news.NewsRequestDTO;
import com.apteka.portal.dtos.request.news.NewsUpdateRequestDTO;
import com.apteka.portal.dtos.response.news.NewsResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.NewsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Новости", description = "Управление новостями групп пользователей")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @Operation(summary = "Новости группы", description = "Возвращает список новостей указанной группы пользователей.")
    @GetMapping("/by-user-group/{id}")
    public ResponseEntity<List<NewsResponseDTO>> getByUserGroup(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(newsService.getByUserGroup(id, currentUser));
    }

    @Operation(summary = "Получить новость по ID", description = "Возвращает одну новость по идентификатору.")
    @GetMapping("/{id}")

    public ResponseEntity<NewsResponseDTO> getOne(@PathVariable Integer id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(newsService.getOne(id, currentUser));
    }

    @Operation(summary = "Создать новость", description = "Создаёт новую новость. Требуется право NEWS_WORK / NEWS_WORK_ALL_GROUPS или роль ADMIN.")
    @PreAuthorize("@security.hasAction('NEWS_WORK') or @security.hasAction('NEWS_WORK_ALL_GROUPS') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(@Valid @RequestBody NewsRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(newsService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить новость", description = "Обновляет существующую новость.")
    @PreAuthorize("""
            @security.hasAction('UPDATE_ALL_NEWS_IN_GROUP')
            or @security.hasAction('UPDATE_ALL_NEWS_CREATE_GROUP')
            or @security.hasAction('UPDATE_ALL_NEWS')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody NewsUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(newsService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить новость", description = "Удаляет новость по идентификатору.")
    @PreAuthorize("""
            @security.hasAction('DELETE_ALL_NEWS_CREATE_GROUP')
            or @security.hasAction('DELETE_ALL_NEWS_IN_GROUP')
            or @security.hasAction('DELETE_ALL_NEWS')
            or @security.hasRole('ADMIN')
                """)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        newsService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
