package com.apteka.portal.config;

import com.apteka.portal.repository.UserGroupRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.Account;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.ClientRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.data-initializer.enabled", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements ApplicationRunner {
    private final UserGroupRepository userGroupRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.default.admin.login}")
    private String adminLogin;

    @Value("${app.default.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String adminGroupName = "Группа администраторов";
        UserGroup adminGroup;

        if (!userGroupRepository.existsByName(adminGroupName)) {
            log.info("Группа администраторов не найдена. Запуск процесса создания...");
            adminGroup = UserGroup.builder().name(adminGroupName).isActive(true).build();
            userGroupRepository.save(adminGroup);
        } else {
            adminGroup = userGroupRepository.findByName(adminGroupName)
                    .orElseThrow(() -> new RuntimeException("Критическая ошибка: Группа не найдена"));
        }

        if (!clientRepository.existsByAccount_Login(adminLogin)) {
            log.info("Начальный администратор не найден. Запуск процесса создания...");

            Client admin = Client.builder()
                    .fullName("Администратор")
                    .build();

            Account account = Account.builder()
                    .login(adminLogin)
                    .password(passwordEncoder.encode(adminPassword))
                    .userGroup(adminGroup)
                    .userRole(UserRole.ADMIN)
                    .isActive(true)
                    .client(admin)
                    .build();

            admin.setAccount(account);
            clientRepository.save(admin);
        }
    }
}
