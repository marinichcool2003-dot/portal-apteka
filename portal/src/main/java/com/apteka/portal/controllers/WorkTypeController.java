package com.apteka.portal.controllers;

import com.apteka.portal.services.WorkTypeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

import com.apteka.portal.docs.BadRequestApiResponse;
import com.apteka.portal.docs.ConflictApiResponse;
import com.apteka.portal.docs.ForbiddenApiResponse;
import com.apteka.portal.docs.InternalServerErrorApiResponse;
import com.apteka.portal.docs.NotFoundApiResponse;
import com.apteka.portal.docs.UnauthorizedApiResponse;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.models.AppUserDetails;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/work-types")
@RequiredArgsConstructor
@PreAuthorize("@appSecurity.isClient()")
@Tag(name = "Виды работ")
public class WorkTypeController {
    private final WorkTypeService workTypeService;

    @Operation(summary = "Получить список видов работ по группе задач")
    @ApiResponse(responseCode = "200", description = "Список видов работ успешно получен")
    @NotFoundApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/by-group-task/{groupTaskId}")
    public ResponseEntity<List<WorkTypeResponseDTO>> getByGroupTask(@PathVariable Integer groupTaskId) {
        return ResponseEntity.ok(workTypeService.getByGroupTask(groupTaskId));
    }

    @Operation(summary = "Получить вид работы по ID")
    @ApiResponse(responseCode = "200", description = "Вид работы успешно получен")
    @NotFoundApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(workTypeService.getOne(id));
    }

    @Operation(summary = "Создать новый вид работы")
    @ApiResponse(responseCode = "201", description = "Вид работы успешно создан")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PostMapping
    public ResponseEntity<WorkTypeResponseDTO> create(@Valid @RequestBody WorkTypeRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(workTypeService.create(dto, currentUser));
    }
    
    @Operation(summary = "Обновить вид работы")
    @ApiResponse(responseCode = "200", description = "Вид работы успешно обновлен")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PutMapping("/{id}")
    public ResponseEntity<WorkTypeResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody WorkTypeRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok().body(workTypeService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить вид работы")
    @ApiResponse(responseCode = "204", description = "Вид работы успешно удален")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        workTypeService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
