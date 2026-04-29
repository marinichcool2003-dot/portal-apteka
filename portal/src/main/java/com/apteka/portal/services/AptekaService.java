package com.apteka.portal.services;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.DublicateAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaNumberException;
import com.apteka.portal.exceptions.InvalidAptekaPasswordException;
import com.apteka.portal.exceptions.InvalidAptekaAdressException;
import com.apteka.portal.exceptions.InvalidAptekaPhoneNumberException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AptekaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AptekaService {
    private final AptekaRepository aptekaRepository;
    private final UserGroupService userGroupService;
    private final PasswordEncoder passwordEncoder;

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
    public List<Apteka> filter(String login, Integer groupId, Integer number, String phoneNumber){
        return aptekaRepository.filter(login, groupId, number, phoneNumber);
    }

    @Transactional
    public Apteka create(String login, String password, Integer number, String adress, String phoneNumber, Integer groupId) {
        if (login == null || login.isBlank())
            throw new InvalidAptekaLoginException();
        if (password == null || password.isBlank())
            throw new InvalidAptekaPasswordException();
        if (number == 0 || number == null)
            throw new InvalidAptekaNumberException();
        if (adress == null)
            throw new InvalidAptekaAdressException();
        if (phoneNumber == null || phoneNumber.strip().length() != 10)
            throw new InvalidAptekaPhoneNumberException();

        login = login.strip();

        if (aptekaRepository.existsByLogin(login))
            throw new DublicateAptekaLoginException(login);

        UserGroup userGroup = userGroupService.getOne(groupId);

        Apteka apteka = Apteka.builder()
                .login(login)
                .password(passwordEncoder.encode(password))
                .number(number)
                .userGroup(userGroup)
                .build();

        return aptekaRepository.save(apteka);
    }

    @Transactional
    public void delete(Integer id) {
        if (!aptekaRepository.existsById(id)) {
            throw new AptekaNotFoundException(id);
        }
        aptekaRepository.deleteById(id);
    }
}
