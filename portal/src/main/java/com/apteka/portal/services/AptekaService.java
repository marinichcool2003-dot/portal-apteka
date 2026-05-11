package com.apteka.portal.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.request.PasswordValidator;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.DublicateAptekaFullNameException;
import com.apteka.portal.exceptions.DublicateAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaNumberException;
import com.apteka.portal.exceptions.InvalidAptekaAdressException;
import com.apteka.portal.exceptions.InvalidAptekaPhoneNumberException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AptekaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AptekaService {
    private final AptekaRepository aptekaRepository;
    private final UserGroupService userGroupService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<Apteka> getAll() {
        return aptekaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Apteka getOne(Integer id) {
        return aptekaRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Apteka> filter(AptekaFilterRequestDTO dto) {
        return aptekaRepository.filter(dto.login(), dto.groupId(), dto.number(), dto.phoneNumber());
    }

    @Transactional
    public Apteka create(AptekaRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        hasAccessToApteki(currentUser);
        validateLogin(dto.login());
        passwordValidator.validatePassword(dto.password(), false);
        UserGroup userGroup = userGroupService.getOne(dto.groupId());
        validateAptekaNumberInGroup(dto.number(), dto.groupId());
        valdiateAptekaAdress(dto.adress());
        validatePhoneNumber(dto.phoneNumber());

        String cleanLogin = dto.login().strip();

        Apteka apteka = Apteka.builder()
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .number(dto.number())
                .userGroup(userGroup)
                .build();

        return aptekaRepository.save(apteka);
    }

    @Transactional
    public Apteka update(Integer id, AptekaRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        hasAccessToApteki(currentUser);

        Apteka apteka = getOne(id);
        String oldLogin = apteka.getLogin();
        boolean needsLogout = false;

        if (dto.login() != null && !dto.login().isBlank()) {
            String newLogin = dto.login().strip();
            if (!newLogin.equals(oldLogin)) {
                validateLogin(newLogin);
                apteka.setLogin(newLogin);
                needsLogout = true;
            }
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            if (passwordEncoder.matches(dto.password(), apteka.getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            passwordValidator.validatePassword(dto.password(), false);
            apteka.setPassword(passwordEncoder.encode(dto.password()));
            needsLogout = true;
        }

        if (dto.adress() != null && !dto.adress().isBlank()) {
            valdiateAptekaAdress(dto.adress());
            apteka.setAdress(dto.adress().strip());
        }

        if (dto.number() != null && dto.number() > 0) {
            if (!dto.number().equals(apteka.getNumber())) {
                validateAptekaNumberInGroup(dto.number(), apteka.getUserGroup().getId());
                apteka.setNumber(dto.number());
            }
        }

        if (dto.groupId() != null && dto.groupId() > 0) {
            UserGroup userGroup = userGroupService.getOne(dto.groupId());
            validateAptekaNumberInGroup(apteka.getNumber(), dto.groupId());
            apteka.setUserGroup(userGroup);
        }

        Apteka savedApteka = aptekaRepository.save(apteka);

        if (needsLogout) {
            authService.invalidateAllSession(oldLogin);
        }

        return savedApteka;
    }

    @Transactional
    public void delete(Integer id) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        hasAccessToApteki(currentUser);
        if (!aptekaRepository.existsById(id)) {
            throw new AptekaNotFoundException(id);
        }
        aptekaRepository.deleteById(id);
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new InvalidAptekaLoginException("Логин аптеки не может быть пустым");
        }
        if (!login.contains("@farmp.ru")) {
            throw new InvalidAptekaLoginException("Логин аптеки должен содержать домен \"@farmp.ru\"");
        }
        if (aptekaRepository.existsByLogin(login)) {
            throw new DublicateAptekaLoginException("Аптека с данным логином уже существует");
        }
    }

    private void validateAptekaNumberInGroup(Integer number, Integer groupId) {
        if (number == 0 || number == null) {
            throw new InvalidAptekaNumberException();
        }
        if (aptekaRepository.existsByUserGroupAndNumber(groupId, number)) {
            throw new DublicateAptekaFullNameException("Аптека с таким юридическим лицом и номером уже существует");
        }
    }

    private void valdiateAptekaAdress(String adress) {
        if (adress == null) {
            throw new InvalidAptekaAdressException("Адрес аптеки не может быть пустым");
        }
        if (!adress.matches(".*[a-zA-Zа-яА-Я].*") || !adress.matches(".*[0-9].*")) {
            throw new InvalidAptekaAdressException("Адрес должен содержать название улицы и номер дома");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidAptekaPhoneNumberException("Номер телефона не может быть пустым");
        }
        String cleanNumber = phoneNumber.replaceAll("\\D", "");
        if (!cleanNumber.matches("^[78]\\d{10}$")) {
            throw new InvalidAptekaPhoneNumberException("Некорректный формат номера. Ожидается 11 цифр.");
        }
    }

    private void hasAccessToApteki(AppUserDetails currentUser) {
        if (!currentUser.hasAnyRole(UserRole.ADMIN, UserRole.BOSS)) {
            throw new AccessDeniedException("Только администратор или начальник может добавлять аптеки");
        }
    }
}
