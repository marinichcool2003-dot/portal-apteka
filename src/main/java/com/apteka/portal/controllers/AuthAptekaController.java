package com.apteka.portal.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.services.AuthAptekaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class AuthAptekaController {
    private final AuthAptekaService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody AptekaRequestDTO aptekaRequestDTO) {
        String token = authService.register(
            aptekaRequestDTO.login(), 
            aptekaRequestDTO.password(), 
            aptekaRequestDTO.number(),
            aptekaRequestDTO.adress(),
            aptekaRequestDTO.phoneNumber(),
            aptekaRequestDTO.groupId()
        );
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String login, @RequestParam String password) {
        String token = authService.login(login, password);
        return ResponseEntity.ok(token);
    }

}
