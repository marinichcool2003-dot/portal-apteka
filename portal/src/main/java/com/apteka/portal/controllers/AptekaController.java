package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.docs.BadRequestApiResponse;
import com.apteka.portal.docs.ConflictApiResponse;
import com.apteka.portal.docs.ForbiddenApiResponse;
import com.apteka.portal.docs.InternalServerErrorApiResponse;
import com.apteka.portal.docs.NotFoundApiResponse;
import com.apteka.portal.docs.UnauthorizedApiResponse;
import com.apteka.portal.dtos.request.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;
import com.apteka.portal.services.AptekaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @ApiResponse(responseCode = "200", description = "Список аптек успешно получен")
    @InternalServerErrorApiResponse
    @GetMapping
    public ResponseEntity<List<AptekaResponseDTO>> getAll() {
        return ResponseEntity.ok(aptekaService.getAll());
    }

    @Operation(summary = "Получить аптеку по ID")
    @ApiResponse(responseCode = "200", description = "Аптека успешно получена")
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(aptekaService.getOne(id));
    }

    @Operation(summary = "Получить текущую аптеку")
    @ApiResponse(responseCode = "200", description = "Текущая аптека успешно получена")
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/me")
    public ResponseEntity<AptekaResponseDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.getOne(currentUser.getAptekaId()));
    }

    @Operation(summary = "Фильтрация аптек")
    @ApiResponse(responseCode = "200", description = "Фильтрация успешно выполнена")
    @BadRequestApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/filter")
    public ResponseEntity<List<AptekaResponseDTO>> filter(@Parameter @Valid @ModelAttribute AptekaFilterRequestDTO dto, Pageable pageable) {
        return ResponseEntity.ok().body(aptekaService.filter(dto, pageable));
    }


    @Operation(summary = "Создать аптеку")
    @ApiResponse(responseCode = "201", description = "Аптека успешно создана")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@Valid @RequestBody AptekaRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aptekaService.create(dto, currentUser));
    }


    @Operation(summary = "Обновить аптеку")
    @ApiResponse(responseCode = "200", description = "Аптека успешно обновлена")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PutMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> update(@PathVariable Integer id, @Valid @RequestBody AptekaUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(aptekaService.update(id, dto, currentUser));
    }

    @Operation(summary = "Удалить аптеку")
    @ApiResponse(responseCode = "204", description = "Аптека успешно удалена")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id, @AuthenticationPrincipal AppUserDetails currentUser) {
        aptekaService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}