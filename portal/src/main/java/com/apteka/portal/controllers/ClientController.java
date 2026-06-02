package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.docs.BadRequestApiResponse;
import com.apteka.portal.docs.ConflictApiResponse;
import com.apteka.portal.docs.ForbiddenApiResponse;
import com.apteka.portal.docs.InternalServerErrorApiResponse;
import com.apteka.portal.docs.NotFoundApiResponse;
import com.apteka.portal.docs.UnauthorizedApiResponse;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@PreAuthorize("@appSecurity.isClient()")
@Tag(name = "Пользователи")
public class ClientController {
    private final ClientService clientService;

    @Operation(summary = "Получить список сотрудников")
    @ApiResponse(responseCode = "200", description = "Список сотрудников успешно получен")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @InternalServerErrorApiResponse
    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAll(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getAll(currentUser));
    }

    @Operation(summary = "Получить сотрудника по ID")
    @ApiResponse(responseCode = "200", description = "Сотрудник успешно получен")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getOne(@PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(id, currentUser));
    }

    @Operation(summary = "Получить текущего сотрудника")
    @ApiResponse(responseCode = "200", description = "Текущий сотрудник успешно получен")
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/me")
    public ResponseEntity<ClientResponseDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(currentUser.getClientId(), currentUser));
    }

    @Operation(summary = "Получить статистику задач текущего сотрудника")
    @GetMapping("/my-stats")
    @ApiResponse(responseCode = "200", description = "Cтатистика успешно получена")
    @UnauthorizedApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    public ResponseEntity<TaskStatsDTO> getMyStats(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getMyStats(currentUser));
    }

    @Operation(summary = "Получить сотрудников по группе")
    @ApiResponse(responseCode = "200", description = "Сотрудники успешно получены")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/by-user-group/{userGroupId}")
    public ResponseEntity<List<ClientResponseDTO>> getByGroup(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getByGroup(userGroupId, currentUser));
    }

    @Operation(summary = "Получить статистику сотрудников по задачам")
    @ApiResponse(responseCode = "200", description = "Статистика успешно получена")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @GetMapping("/by-user-group/task-number/{userGroupId}")
    public ResponseEntity<List<ClientWithStatsDTO>> getWithNumberOfTask(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getWithNumberOfTask(userGroupId, currentUser));
    }

    @Operation(summary = "Создать сотрудника")
    @ApiResponse(responseCode = "201", description = "Сотрудник успешно создан")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.create(dto, currentUser));
    }

    @Operation(summary = "Добавить роль (Только для ADMIN или BOSS)")
    @ApiResponse(responseCode = "200", description = "Роль успешно добавлена")
    @NotFoundApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @InternalServerErrorApiResponse
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @PutMapping("/add-role/{id}")
    public ResponseEntity<ClientResponseDTO> addRole(@PathVariable UUID id, @RequestParam String role, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.addRole(id, role, currentUser));
    }

    @Operation(summary = "Добавить роль (Только для ADMIN или BOSS)")
    @ApiResponse(responseCode = "200", description = "Роль успешно удалена")
    @NotFoundApiResponse
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @InternalServerErrorApiResponse
    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @PutMapping("/remove-role/{id}")
    public ResponseEntity<ClientResponseDTO> removeRole(@PathVariable UUID id, @RequestParam String role, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.removeRole(id, role, currentUser));
    }

    @Operation(summary = "Обновить собственный профиль")
    @ApiResponse(responseCode = "200", description = "Профиль успешно обновлен")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PutMapping(value = "/update-yourself", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientResponseDTO> updateYourself(@AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @ModelAttribute ClientUpdateRequestDTO dto) throws IOException {
        return ResponseEntity.ok(clientService.updateYourself(dto, currentUser));
    }

    @Operation(summary = "Полное обновление сотрудника")
    @ApiResponse(responseCode = "200", description = "Сотрудник успешно обновлен")
    @BadRequestApiResponse
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @ConflictApiResponse
    @InternalServerErrorApiResponse
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/full-update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClientResponseDTO> fullUpdate(
            @PathVariable UUID id,

            @Valid @ParameterObject @ModelAttribute FullClientUpdateRequestDTO dto,

            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {

        return ResponseEntity.ok(clientService.fullUpdate(id, dto, currentUser));
    }

    @Operation(summary = "Удалить сотрудника")
    @ApiResponse(responseCode = "204", description = "Сотрудник успешно удален")
    @UnauthorizedApiResponse
    @ForbiddenApiResponse
    @NotFoundApiResponse
    @InternalServerErrorApiResponse
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
