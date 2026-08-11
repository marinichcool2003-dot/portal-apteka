package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.apteka.portal.dtos.response.client.ClientResponseWithActionsDTO;
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
import com.apteka.portal.dtos.response.AccountActionResponseDTO;
import com.apteka.portal.dtos.response.client.ClientResponseDTO;
import com.apteka.portal.dtos.response.client.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.notification.NotificationPreferenceResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.ClientService;
import com.apteka.portal.services.NotificationPreferenceService;

import jakarta.validation.Valid;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientCreateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientFilterRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateDescriptionRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateFullRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdatePersonalProfileRequestDTO;
import com.apteka.portal.dtos.request.notification.NotificationPreferenceUpdateRequestDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Клиенты (сотрудники)", description = "Управление сотрудниками: список, фильтр, профиль, права и удаление")
@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;
    private final NotificationPreferenceService notificationPreferenceService;

    @Operation(summary = "Список клиентов", description = "Возвращает постраничный список клиентов (сотрудников) с фильтром по активности.")
    @GetMapping
    public ResponseEntity<Page<ClientResponseDTO>> getAll(@AuthenticationPrincipal AppUserDetails currentUser,
            Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getAll(currentUser, pageable, isActive));
    }

    @Operation(summary = "Получить клиента по ID", description = "Возвращает данные одного клиента по идентификатору.")
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseWithActionsDTO> getOne(@PathVariable UUID id,
                                                               @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(id, currentUser));
    }

    @Operation(summary = "Текущий клиент", description = "Возвращает профиль текущего авторизованного клиента.")
    @GetMapping("/me")
    public ResponseEntity<ClientResponseWithActionsDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(currentUser.getInternalId(), currentUser));
    }

    @Operation(summary = "Мои настройки уведомлений", description = "Возвращает настройки уведомлений текущего пользователя. По умолчанию все выключены.")
    @GetMapping("/me/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponseDTO> getNotificationPreferences(
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(notificationPreferenceService.getForCurrentUser(currentUser));
    }

    @Operation(summary = "Обновить настройки уведомлений", description = "Сохраняет настройки уведомлений текущего пользователя.")
    @PutMapping("/me/notification-preferences")
    public ResponseEntity<NotificationPreferenceResponseDTO> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferenceUpdateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(
                notificationPreferenceService.updateForCurrentUser(currentUser, dto.preferences()));
    }

    @Operation(summary = "Моя статистика по задачам", description = "Возвращает статистику задач текущего клиента.")
    @GetMapping("/my-stats")
    public ResponseEntity<TaskStatsDTO> getMyStats(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getMyStats(currentUser));
    }

    @Operation(summary = "Клиенты группы", description = "Возвращает постраничный список клиентов указанной группы пользователей.")
    @GetMapping("/by-user-group/{userGroupId}")
    public ResponseEntity<Page<ClientResponseDTO>> getByGroup(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser, Pageable pageable,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getByGroup(userGroupId, currentUser, pageable, isActive));
    }

    @Operation(summary = "Клиенты группы со статистикой задач", description = "Возвращает клиентов группы с количеством задач. Требуются права на просмотр статистики.")
    @GetMapping("/by-user-group/task-number/{userGroupId}")
    @PreAuthorize("@security.hasAction('CAN_SELECT_CLIENT_STATS_IN_GROUP') or @security.hasAction('CAN_SELECT_CLIENT_STATS_GRAND') or @security.hasRole('ADMIN')")
    public ResponseEntity<List<ClientWithStatsDTO>> getWithNumberOfTask(@PathVariable Integer userGroupId,
            @AuthenticationPrincipal AppUserDetails currentUser,
            @RequestParam(defaultValue = "true") Boolean isActive) {
        return ResponseEntity.ok(clientService.getWithNumberOfTask(userGroupId, currentUser, isActive));
    }

    @Operation(summary = "Фильтр клиентов", description = "Поиск и фильтрация клиентов по параметрам запроса с постраничной выдачей.")
    @GetMapping("/filter")
    public ResponseEntity<Page<ClientResponseDTO>> filter(@ModelAttribute ClientFilterRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser, Pageable pageable) {
        return ResponseEntity.ok(clientService.filter(dto, currentUser, pageable));
    }

    @Operation(summary = "Создать клиента", description = "Создаёт нового клиента (сотрудника) в группе.")
    @PreAuthorize("@security.hasAction('CREATE_CLIENT_IN_GROUP') or @security.hasAction('CREATE_CLIENT_GRAND') or @security.hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientCreateRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.create(dto, currentUser));
    }

    @Operation(summary = "Обновить учётную запись клиента", description = "Обновляет данные аккаунта клиента (логин/пароль и связанные поля).")
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

    @Operation(summary = "Обновить описание клиента", description = "Обновляет описание (профильные текстовые поля) клиента.")
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

    @Operation(summary = "Обновить личный профиль", description = "Обновляет персональный профиль текущего клиента (включая аватар при наличии).")
    @PutMapping("/update-profile")
    public ResponseEntity<ClientResponseDTO> updateProfile(@Valid ClientUpdatePersonalProfileRequestDTO dto,
            @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.ok(clientService.updatePersonalProfile(dto, currentUser));
    }

    @Operation(summary = "Полное обновление клиента", description = "Обновляет все данные клиента целиком.")
    @PreAuthorize("@security.hasAction('UPDATE_CLIENT_IN_GROUP_GRAND') or @security.hasAction('UPDATE_CLIENT_GRAND') or @security.hasRole('ADMIN')")
    @PutMapping("/update-all/{id}")
    public ResponseEntity<ClientResponseDTO> updateAll(@PathVariable UUID id,
            @Valid @RequestBody ClientUpdateFullRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser)
            throws IOException {
        return ResponseEntity.ok(clientService.updateFullClient(id, dto, currentUser));
    }

    @Operation(
            summary = "Действия, доступные к назначению",
            description = """
                    Возвращает каталог AccountAction, которые текущий пользователь может назначать другим.
                    Требуется право CAN_ADD_ACCOUNT_ACTIONS_* или роль ADMIN.
                    USER — только уровень LOW; BOSS — LOW и MEDIUM; ADMIN / GRAND_EXTENDED — все уровни.
                    """)
    @PreAuthorize("""
            @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_IN_GROUP')
            or @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_GRAND')
            or @security.hasAction('CAN_ADD_ACCOUNT_ACTIONS_GRAND_EXTENDED')
            or @security.hasRole('ADMIN')
                """)
    @GetMapping("/assignable-actions")
    public ResponseEntity<Set<AccountActionResponseDTO>> getAssignableActions(
            @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getAssignableActions(currentUser));
    }

    @Operation(summary = "Добавить права клиенту", description = "Назначает клиенту набор действий (actions) по кодам.")
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

    @Operation(summary = "Удалить права у клиента", description = "Снимает у клиента набор действий (actions) по кодам.")
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

    @Operation(summary = "Мягкое удаление клиента", description = "Деактивирует клиента (safe delete) без физического удаления из БД.")
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

    @Operation(summary = "Восстановить клиента", description = "Восстанавливает клиента после мягкого удаления.")
    @PreAuthorize("@security.hasAction('CAN_ACTIVATE_CLIENT_AFTER_SAFE_DELETE') or @security.hasRole('ADMIN')")
    @PatchMapping("/restore-after-safe-delete/{id}")
    public ResponseEntity<Void> restore(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.restoreAfterSafeDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Полное удаление клиента", description = "Безвозвратно удаляет клиента из системы.")
    @PreAuthorize("@security.hasAction('PERMANENT_DELETE_CLIENT') or @security.hasRole('ADMIN')")
    @DeleteMapping("/permanent-delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.permanentDelete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
