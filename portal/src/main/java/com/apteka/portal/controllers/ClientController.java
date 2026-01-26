package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.ClientAvatarRequestDTO;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.models.Client;
import com.apteka.portal.services.ClientService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(clientService.getOne(id));
    }

    @PostMapping
    public ResponseEntity<Client> create(@RequestBody ClientRequestDTO clientRequestDTO) throws IOException{
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(clientService.create(
                        clientRequestDTO.login(),
                        clientRequestDTO.password(),
                        clientRequestDTO.fullName(),
                        clientRequestDTO.role(),
                        clientRequestDTO.groupClient()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Client> updateAvatar(@PathVariable UUID id, @RequestBody ClientAvatarRequestDTO clientAvatarRequestDTO) throws IOException{
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientService.updateAvatar(
                    id, 
                    clientAvatarRequestDTO.avatar()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable UUID id, @RequestBody ClientRequestDTO clientRequestDTO){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(clientService.update(
                        id, 
                        clientRequestDTO.login(), 
                        clientRequestDTO.password(), 
                        clientRequestDTO.role(), 
                        clientRequestDTO.groupClient()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        clientService.delete(id);
        return ResponseEntity
            .noContent()
            .build();
    }
}
