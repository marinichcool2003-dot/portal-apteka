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
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

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
    @Transactional // Обязательно добавляем для работы ленивой инициализации и автоматического UPDATE
    public void run(ApplicationArguments args) throws Exception {
        String adminGroupName = "Группа администраторов";

        // 1. Получаем или создаем группу администраторов (без избыточных save)
        UserGroup adminGroup = userGroupRepository.findByName(adminGroupName)
                .orElseGet(() -> {
                    log.info("Группа администраторов не найдена. Запуск процесса создания...");

                    UserGroup newGroup = UserGroup.builder()
                            .name(adminGroupName)
                            .isActive(true)
                            // AUDIT-FIX: группа администраторов — группа сотрудников
                            .groupType(com.apteka.portal.models.UserGroupType.EMPLOYEE_GROUP)
                            .build();

                    // Сохраняем и принудительно отправляем в БД, чтобы сгенерировался ID
                    UserGroup saved = userGroupRepository.saveAndFlush(newGroup);

                    // Устанавливаем связь на саму себя
                    saved.setVisibleGroups(Set.of(saved));

                    // Благодаря @Transactional, метод save() здесь второй раз вызывать НЕ нужно!
                    // Hibernate сам сделает UPDATE перед коммитом транзакции.
                    return saved;
                });

        // 2. Получаем или создаем аккаунт администратора
        if (!clientRepository.existsByAccount_Login(adminLogin)) {
            log.info("Начальный администратор не найден. Запуск процесса создания...");

            Client admin = Client.builder()
                    .fullName("Администратор")
                    .build();

            Account account = Account.builder()
                    .login(adminLogin)
                    // AUDIT-FIX: email администратора по умолчанию
                    .email("admin@farmp.ru")
                    .password(passwordEncoder.encode(adminPassword))
                    .userGroup(adminGroup)
                    .userRole(UserRole.ADMIN)
                    .isActive(true)
                    .client(admin)
                    .build();

            admin.setAccount(account);

            // Сохраняем клиента (вместе с ним по каскаду сохранится и Account)
            clientRepository.save(admin);
        }
    }
}
