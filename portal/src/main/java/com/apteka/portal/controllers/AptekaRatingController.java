package com.apteka.portal.controllers;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.rating.AptekaTaskRatingCreateRequestDTO;
import com.apteka.portal.dtos.request.rating.AptekaTaskRatingUpdateRequestDTO;
import com.apteka.portal.dtos.response.rating.AptekaRatingsPageResponseDTO;
import com.apteka.portal.dtos.response.rating.AptekaTaskRatingResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.AptekaRatingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Оценки аптек", description = "Оценка работы аптеки по закрытым и отклонённым задачам")
@RestController
@RequiredArgsConstructor
public class AptekaRatingController {
    private final AptekaRatingService aptekaRatingService;

    @Operation(summary = "Создать оценку по задаче",
            description = "Исполнитель или администратор выставляет оценку аптеке-создателю задачи. Доступно только для закрытых или отклонённых задач.")
    @PostMapping("/api/v1/tasks/{taskId}/rating")
    public ResponseEntity<AptekaTaskRatingResponseDTO> create(
            @PathVariable Long taskId,
            @Valid @RequestBody AptekaTaskRatingCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaRatingService.create(taskId, dto, currentUser));
    }

    @Operation(summary = "Обновить оценку по задаче",
            description = "Исполнитель может изменить оценку один раз; администратор — без ограничений.")
    @PutMapping("/api/v1/tasks/{taskId}/rating")
    public ResponseEntity<AptekaTaskRatingResponseDTO> update(
            @PathVariable Long taskId,
            @Valid @RequestBody AptekaTaskRatingUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaRatingService.update(taskId, dto, currentUser));
    }

    @Operation(summary = "Получить оценку по задаче",
            description = "Доступно сотрудникам (USER, BOSS, ADMIN). Пользователям аптеки — запрещено.")
    @GetMapping("/api/v1/tasks/{taskId}/rating")
    public ResponseEntity<AptekaTaskRatingResponseDTO> getByTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaRatingService.getByTask(taskId, currentUser));
    }

    @Operation(summary = "Список оценок аптеки",
            description = "Постраничный список оценок аптеки со средней оценкой и общим количеством. Только для сотрудников офиса.")
    @GetMapping("/api/v1/apteka/{id}/ratings")
    public ResponseEntity<AptekaRatingsPageResponseDTO> getByApteka(
            @PathVariable UUID id,
            Pageable pageable,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaRatingService.getByApteka(id, pageable, currentUser));
    }
}
