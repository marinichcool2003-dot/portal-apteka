package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;
import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.ClientService;

import jakarta.validation.Valid;

import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@PreAuthorize("@appSecurity.isClient()")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponseDTO>> getAll(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getAll(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDTO> getOne(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(id, currentUser));
    }

    @GetMapping("/me")
    public ResponseEntity<ClientResponseDTO> getMe(@AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getOne(currentUser.getClientId(), currentUser));
    }

    @GetMapping("/by-user-group/{userGroupId}")
    public ResponseEntity<List<ClientResponseDTO>> getByGroup(@PathVariable Integer userGroupId, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getByGroup(userGroupId, currentUser));
    }

    @GetMapping("/by-user-group/task-number/{userGroupId}")
    public ResponseEntity<List<ClientWithStatsDTO>> getWithNumberOfTask(@PathVariable Integer userGroupId, @AuthenticationPrincipal AppUserDetails currentUser) {
        return ResponseEntity.ok(clientService.getWithNumberOfTask(userGroupId, currentUser));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'BOSS')")
    @PostMapping
    public ResponseEntity<ClientResponseDTO> create(@Valid @RequestBody ClientRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.create(dto, currentUser));
    }

    @PutMapping("/update-yourself")
    public ResponseEntity<ClientResponseDTO> updateYourself(@AuthenticationPrincipal AppUserDetails currentUser,
            @Valid @ModelAttribute ClientUpdateRequestDTO dto) throws IOException {
        UUID clientId = currentUser.getClientId();
        return ResponseEntity.ok(clientService.updateYourself(clientId, dto, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/full-update/{id}")
    public ResponseEntity<ClientResponseDTO> fullUpdate(@PathVariable UUID id,
            @Valid @ModelAttribute FullClientUpdateRequestDTO dto, @AuthenticationPrincipal AppUserDetails currentUser) throws IOException {
        return ResponseEntity.ok(clientService.fullUpdate(id, dto, currentUser));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, @AuthenticationPrincipal AppUserDetails currentUser) {
        clientService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
