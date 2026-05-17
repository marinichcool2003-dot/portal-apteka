package com.apteka.portal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.Client;
import com.apteka.portal.repository.ClientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;   

    @Override
    public void run(String... args) throws Exception {
        String adminLogin = "portalAdmin";

        if (!clientRepository.existsByLogin(adminLogin)) {
            log.info("Начальный эадминистратор не найден. Запуск процесса создания...");

            Client admin = Client.builder()
                    .login(adminLogin)
                    .password(passwordEncoder.encode("ad"))
        }

    }
}
