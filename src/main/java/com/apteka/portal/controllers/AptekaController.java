package com.apteka.portal.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.services.AptekaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/apteka")
@RequiredArgsConstructor
public class AptekaController {
    private final AptekaService aptekaService;

    @GetMapping
    public ResponseEntity<List<AptekaResponseDTO>> getAll() {
        var apteki = aptekaService.getAll().stream()
                .map(AptekaResponseDTO::from)
                .toList();
        return ResponseEntity.ok(apteki);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AptekaResponseDTO> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(AptekaResponseDTO.from(aptekaService.getOne(id)));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<AptekaResponseDTO>> filter(
            @RequestParam(required = false) String login,
            @RequestParam(required = false) Integer groupId,
            @RequestParam(required = false) Integer number,
            @RequestParam(required = false) String phoneNumber
        ) {
        var apteki = aptekaService.filter(login, groupId, number, phoneNumber)
            .stream()
            .map(AptekaResponseDTO::from)
            .toList();
        return ResponseEntity.ok(apteki);
    }
    
    @PostMapping
    public ResponseEntity<AptekaResponseDTO> create(@RequestBody AptekaRequestDTO aptekaRequestDTO) {
        var createdApteka = aptekaService.create(
                aptekaRequestDTO.login(),
                aptekaRequestDTO.password(),
                aptekaRequestDTO.number(),
                aptekaRequestDTO.adress(),
                aptekaRequestDTO.phoneNumber(),
                aptekaRequestDTO.groupId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(AptekaResponseDTO.from(createdApteka));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        aptekaService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
