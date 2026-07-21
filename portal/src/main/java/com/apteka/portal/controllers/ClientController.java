package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.dtos.response.client.ClientResponseDTO;
import com.apteka.portal.dtos.response.client.ClientWithStatsDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientCreateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientFilterRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateDescriptionRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateFullRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdatePersonalProfileRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Пользователи")
public class ClientController {
    private final ClientService clientService;

    @Operation(summary = "Получить список сотрудников")
    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> getAll(@AuthenticationPrincipal AppUserDetails currentUser,
            Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getAll(currentUser, pageable, isActive));
    }

    @Operation(summary = "Получить сотрудника по ID")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getOne(@PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(id, currentUser));
    }

    @Operation(summary = "Получить текущего сотрудника")
    @GetMapping("/me")
    public ResponseEntity<ClientResponseDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(currentUser.getInternalId(), currentUser));
    }

    @Operation(summary = "Получить статистику задач текущего сотрудника")
    @GetMapping("/my-stats")
    public ResponseEntity<TaskStatsDTO> getMyStats(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getMyStats(currentUser));
    }

    @Operation(summary = "Получить сотрудников по группе")
    @GetMapping("/by-user-group/{userGroupId}")
    public ResponseEntity<Page<ClientResponseDTO>> getByGroup(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser, Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getByGroup(userGroupId, currentUser, pageable, isActive));
    }

    @Operation(summary = "Получить статистику сотрудников по задачам")
    @GetMapping("/by-user-group/task-number/{userGroupId}")
    @PreAuthorize("@security.hasAction('CAN_SELECT_CLIENT_STATS_IN_GROUP') or @security.hasAction('CAN_SELECT_CLIENT_STATS_GRAND') or @security.hasRole('ADMIN')")
    public ResponseEntity<List<ClientWithStatsDTO>> getWithNumberOfTask(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getWithNumberOfTask(userGroupId, currentUser, isActive));
    }

    @Operation(summary = "Фильтр сотрудников")
    @GetMapping("/filter")
    public ResponseEntity<Page<ClientResponseDTO>> filter(@ModelAttribute ClientFilterRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser, Pageable pageable) {
        return ResponseEntity.ok(clientService.filter(dto, currentUser, pageable));
    }

    @Operation(summary = "Создать сотрудника")
    @PreAuthorize("@security.hasAction('CREATE_CLIENT_IN_GROUP') or @security.hasAction('CREATE_CLIENT_GRAND') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить аккаунт сотрудника")
    @PreAuthorize("""
            @security.hasAction('UPDATE_CLIENT_ACCOUNT_IN_GROUP')
            or @security.hasAction('UPDATE_CLIENT_ACCOUNT_GRAND')
            or @security.hasAction('UPDATE_CLIENT_GRAND')
            or @security.hasAction('UPDATE_CLIENT_IN_GROUP_GRAND')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/update-account/{id}")
    public ResponseEntity<ClientResponseDTO> updateAccount(@PathVariable UUID id,
            @Valid @RequestBody AccountUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.updateAccount(id, dto, currentUser));
    }

    @Operation(summary = "Обновить описание сотрудника")
    @PreAuthorize("""
            @security.hasAction('UPDATE_CLIENT_DESCRIPTION_IN_GROUP')
            or @security.hasAction('UPDATE_CLIENT_DESCRIPTION_GRAND')
            or @security.hasAction('UPDATE_CLIENT_IN_GROUP_GRAND')
            or @security.hasAction('UPDATE_CLIENT_GRAND')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/update-description/{id}")
    public ResponseEntity<ClientResponseDTO> updateDescription(@PathVariable UUID id,
            @Valid @RequestBody ClientUpdateDescriptionRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.updateClientDescription(id, dto, currentUser));
    }

    @Operation(summary = "Обновить свой профиль")
    @PutMapping("/update-profile")
    public ResponseEntity<ClientResponseDTO> updateProfile(@Valid ClientUpdatePersonalProfileRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.ok(clientService.updatePersonalProfile(dto, currentUser));
    }

    @Operation(summary = "Полное обновление сотрудника")
    @PreAuthorize("@security.hasAction('UPDATE_CLIENT_IN_GROUP_GRAND') or @security.hasAction('UPDATE_CLIENT_GRAND') or @security.hasRole('ADMIN')")
    @PutMapping("/update-all/{id}")
    public ResponseEntity<ClientResponseDTO> updateAll(@PathVariable UUID id,
            @Valid @RequestBody ClientUpdateFullRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser)
            throws IOException {
        return ResponseEntity.ok(clientService.updateFullClient(id, dto, currentUser));
    }

    @Operation(summary = "Добавить действия сотруднику")
    @PreAuthorize("""
            @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_IN_GROUP')
            or @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_GRAND')
            or @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_GRAND_EXTENDED')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/add-actions/{id}")
    public ResponseEntity<ClientResponseDTO> addRole(@PathVariable UUID id, @RequestBody Set<String> actionsCode,
            @AuthenticationPrincipal AppUserDetails currentUser) {

        clientService.addActionsToClient(id, actionsCode, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Добавить действия сотруднику")
    @PreAuthorize("""
            @security.hasAction('CAN_REMOVE_ACCOUNT_ACTIONS_IN_GROUP')
            or @security.hasAction('CAN_REMOVE_ACCOUNT_ACTIONS_GRAND')
            or @security.hasAction('CAN_REMOVE_ACCOUNT_ACTIONS_GRAND_EXTENDED')
            or @security.hasRole('ADMIN')
                """)
    @PutMapping("/remove-actions/{id}")
    public ResponseEntity<ClientResponseDTO> removeRole(@PathVariable UUID id, @RequestBody Set<String> actionsCode,
            @AuthenticationPrincipal AppUserDetails currentUser) {

        clientService.removeActionsClient(id, actionsCode, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Безопасное удаление сотрудника")
    @PreAuthorize("""
            @security.hasAction('SAFE_DELETE_CLIENT_IN_GROUP')
            or @security.hasAction('SAFE_DELETE_CLIENT_GRAND')
            or @security.hasAction('PERMANENT_DELETE_CLIENT')
            or @security.hasRole('ADMIN')
                """)
    @PatchMapping("/safe-delete/{id}")
    public ResponseEntity<Void> safeDelete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.safeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Восстановление сотрудника после удаления")
    @PreAuthorize("@security.hasAction('CAN_ACTIVATE_CLIENT_AFTER_SAFE_DELETE') or @security.hasRole('ADMIN')")
    @PatchMapping("/restore-after-safe-delete/{id}")
    public ResponseEntity<Void> restore(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.restoreAfterSafeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перманентное удаление сотрудника")
    @PreAuthorize("@security.hasAction('PERMANENT_DELETE_CLIENT') or @security.hasRole('ADMIN')")
    @DeleteMapping("/permanent-delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.permanentDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
