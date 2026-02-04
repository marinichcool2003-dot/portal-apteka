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
        // Используем AptekaService для создания аптеки
        Apteka apteka = aptekaService.create(login, passwordEncoder.encode(password), number, adress, phoneNumber, groupId);
        
        // Генерируем токен для аптеки (нужно адаптировать JwtTokenProvider для Apteka)
        // Создаем временный Client объект или адаптируем JwtTokenProvider
        // return jwtTokenProvider.generateToken(apteka.getLogin());
        
        // Пока возвращаем просто сообщение
        return "Аптека зарегистрирована. ID: " + apteka.getId();
    }

    public String login(String login, String password) {
        var apteka = aptekaInterface.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Аптека не найдена!"));

        if (!passwordEncoder.matches(password, apteka.getPassword())) {
            throw new RuntimeException("Неверный пароль!");
        }

        // Для аптеки нужно создать отдельный JWT токен
        // return jwtTokenProvider.generateToken(apteka.getLogin());
        
        // Пока возвращаем заглушку
        return "Bearer mock_token_for_apteka_" + apteka.getId();
    }
}