package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.ClientAvatarRequestDTO;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.request.ClientUpdateDTO;
import com.apteka.portal.dtos.request.ClientUpdateRoleDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.models.Client;
import com.apteka.portal.services.ClientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Сотрудники", description = "Операции с сотрудниками")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    @PreAuthorize("hasRole('LEGEND')")
    @Operation(summary = "Получить всех пользователей", description = "Доступно только для ролей LEGEND и ADMIN")
    public ResponseEntity<List<ClientResponseDTO>> getAll() {
        return ResponseEntity
                .ok()
                .body(clientService.getAll()
                        .stream()
                        .map(ClientResponseDTO::from).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('LEGEND')")
    @Operation(summary = "получить пользователя по ID")
    public ResponseEntity<ClientResponseDTO> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(ClientResponseDTO.from(clientService.getOne(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ClientResponseDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Client client = (Client) clientService.loadUserByUsername(username);
        return ResponseEntity.ok(ClientResponseDTO.from(client)); 
    }

    @PatchMapping("/update-avatar-me")
    public ResponseEntity<String> updateAvatarMe(@RequestBody ClientAvatarRequestDTO dto) throws IOException{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        clientService.updateAvatar(authentication.getName(), dto.avatar());
        return ResponseEntity.ok()
            .body("Картинка профиля была успешно изменена!");
    }
    @PostMapping
    @PreAuthorize("hasRole('LEGEND') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDTO> create(@RequestBody ClientRequestDTO dto) throws IOException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ClientResponseDTO.from(
                        clientService.create(
                                dto.login(),
                                dto.password(),
                                dto.fullName(),
                                dto.role(),
                                dto.groupClientId())));
    }

    @PatchMapping("/update-avatar/{id}")
    @PreAuthorize("hasRole('LEGEND') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDTO> updateAvatar(@PathVariable UUID id,
            @RequestBody ClientAvatarRequestDTO dto) throws IOException {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ClientResponseDTO.from(clientService.updateAvatar(
                        id,
                        dto.avatar())));
    }

    @PatchMapping("/change-role/{id}")
    @PreAuthorize("hasRole('LEGEND')")
    public ResponseEntity<ClientResponseDTO> updateRole(@PathVariable UUID id, @RequestBody ClientUpdateRoleDTO dto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ClientResponseDTO.from(clientService.updateRole(
                        id,
                        dto.roleCode())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LEGEND') or hasRole('ADMIN')")
    public ResponseEntity<ClientResponseDTO> update(@PathVariable UUID id, @RequestBody ClientUpdateDTO dto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ClientResponseDTO.from(clientService.update(
                        id,
                        dto.login(),
                        dto.password(),
                        dto.groupClientId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LEGEND')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}