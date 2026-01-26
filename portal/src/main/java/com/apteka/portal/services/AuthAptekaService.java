package com.apteka.portal.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.apteka.portal.models.Apteka;
import com.apteka.portal.repository.AptekaInterface;
import com.apteka.portal.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthAptekaService {
    private final AptekaInterface aptekaInterface;
    private final AptekaService aptekaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public String register(String login, String password, Integer number, String adress, String phoneNumber, Integer groupId) {
        Apteka apteka = aptekaService.create(login, password, number, adress, phoneNumber, groupId);
        return jwtTokenProvider.generateToken(apteka.getLogin());
    }

    public String login(String login, String password) {
        var apteka = aptekaInterface.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Аптека не найдена!"));

        if (!passwordEncoder.matches(password, apteka.getPassword())) {
            throw new RuntimeException("Неверный пароль!");
        }

        return jwtTokenProvider.generateToken(apteka.getLogin());
    }
}
