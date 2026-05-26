package com.apteka.portal.config;

import com.apteka.portal.repository.UserGroupRepository;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.ClientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserGroupRepository userGroupRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.login}")
    private String adminLogin;

    @Value("${app.default.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {

        String adminGroupName = "Группа администраторов";
        UserGroup adminGroup;

        if (!userGroupRepository.existsByName(adminGroupName)) {
            log.info("Группа администраторов не найдена. Запуск процесса создания...");
            adminGroup = UserGroup.builder().name(adminGroupName).build();
            userGroupRepository.save(adminGroup);
        } else {
            adminGroup = userGroupRepository.findByName(adminGroupName)
                .orElseThrow(() -> new RuntimeException("Критическая ошибка: Группа не найдена"));
        }

        if (!clientRepository.existsByLogin(adminLogin)) {
            log.info("Начальный эадминистратор не найден. Запуск процесса создания...");

            Client admin = Client.builder()
                    .login(adminLogin)
                    .password(passwordEncoder.encode(adminPassword))
                    .fullName("Администратор")
                    .roles(Set.of(UserRole.ADMIN))
                    .userGroup(adminGroup)
                    .build();
            clientRepository.save(admin);
        }

    }
}
