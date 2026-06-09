package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.NewsRequestDTO;
import com.apteka.portal.dtos.request.NewsUpdateRequestDTO;
import com.apteka.portal.dtos.response.NewsResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.NewsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Новости")
@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;

    @Operation(summary = "Получить список новостей определенной группы")
    @GetMapping("/by-user-group/{id}")
    public ResponseEntity<List<NewsResponseDTO>> getByUserGroup(@PathVariable Integer id) {
        return ResponseEntity.ok(newsService.getByUserGroup(id));
    }

    @Operation(summary = "Получить новость по id")
    @GetMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(newsService.getOne(id));
    }

    @Operation(summary = "Создать новость")
    @PostMapping
    public ResponseEntity<NewsResponseDTO> create(@RequestBody NewsRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(newsService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить новость")
    @PutMapping("/{id}")
    public ResponseEntity<NewsResponseDTO> update(@PathVariable Integer id,  @RequestBody NewsUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(newsService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить новость")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        newsService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
