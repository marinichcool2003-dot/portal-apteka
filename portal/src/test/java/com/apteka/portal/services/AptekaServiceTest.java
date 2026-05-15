package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apteka.portal.components.PasswordValidator;
import com.apteka.portal.components.SecurityUtils;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.UserGroupRepository;

@ExtendWith(MockitoExtension.class)
public class AptekaServiceTest {
    @Mock
    private AptekaRepository aptekaRepository;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthService authService;
    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private AptekaService aptekaService;

    @Test
    void create_Succesful() {
        AppUserDetails currentUser = TestData.mockJustAdmin();
        UserGroup group = UserGroup.builder().id(4).name("САК").build();
        AptekaRequestDTO dto = new AptekaRequestDTO("  sakapteka123@farmp.ru",
                "ddfKK925_!",
                123,
                " город Ростов-на-Дону,    ул Космонатов 11",
                "+79891112233",
                group.getId());
        Apteka savedApteka = Apteka.builder()
                .id(3)
                .login("sakapteka123@farmp.ru")
                .password("Hashed_password")
                .number(123)
                .adress("город Ростов-на-Дону, ул Космонатов 11")
                .phoneNumber("79891112233")
                .userGroup(group)
                .build();

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(aptekaRepository.existsByUserGroup_IdAndNumber(group.getId(), dto.number())).thenReturn(false);
            when(userGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));
            when(passwordEncoder.encode(any(String.class))).thenReturn("Hashed_password");
            when(aptekaRepository.save(any(Apteka.class))).thenReturn(savedApteka);

            AptekaResponseDTO result = aptekaService.create(dto);

            assertNotNull(result);
            assertEquals(savedApteka.getLogin(), result.login());
            assertEquals(savedApteka.getAdress(), result.adress());
            assertEquals(savedApteka.getPhoneNumber(), result.phoneNumber());

            verify(aptekaRepository, times(1)).existsByUserGroup_IdAndNumber(group.getId(), dto.number());
            verify(userGroupRepository, times(1)).findById(group.getId());
            verify(passwordEncoder, times(1)).encode(anyString());
            verify(aptekaRepository, times(1)).save(any(Apteka.class));
        }
    }

    @Test
    void update_Succesful_WithLoginAndPasswordChange() {
        AppUserDetails currentUser = TestData.mockJustAdmin();
        UserGroup group = UserGroup.builder().id(4).name("САК").build();

        AptekaRequestDTO dto = new AptekaRequestDTO(
                "  new_sakapteka123@farmp.ru",
                "new_password_123!",
                123,
                "город Ростов-на-Дону, ул Космонатов 11",
                "+79891112233",
                group.getId());

        String oldLogin = "sakapteka123@farmp.ru";
        Apteka existingApteka = Apteka.builder()
                .id(3)
                .login(oldLogin)
                .password("old_hashed_password")
                .number(100)
                .adress("Старый адрес")
                .phoneNumber("79991112233")
                .userGroup(group)
                .build();

        Apteka savedApteka = Apteka.builder()
                .id(3)
                .login("new_sakapteka123@farmp.ru")
                .password("new_hashed_password")
                .number(123)
                .adress("город Ростов-на-Дону, ул Космонатов 11")
                .phoneNumber("79891112233")
                .userGroup(group)
                .build();

        try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);

            when(aptekaRepository.findById(existingApteka.getId())).thenReturn(Optional.of(existingApteka));

            when(passwordEncoder.matches(eq(dto.password()), anyString())).thenReturn(false);
            when(passwordEncoder.encode(dto.password())).thenReturn("new_hashed_password");
            when(userGroupRepository.findById(group.getId())).thenReturn(Optional.of(group));

            when(aptekaRepository.save(any(Apteka.class))).thenReturn(savedApteka);

            AptekaResponseDTO result = aptekaService.update(existingApteka.getId(), dto);

            assertNotNull(result);
            assertEquals("new_sakapteka123@farmp.ru", result.login());
            assertEquals("город Ростов-на-Дону, ул Космонатов 11", result.adress());
            assertEquals("79891112233", result.phoneNumber());

            verify(authService, times(1)).invalidateAllSession(oldLogin);
            verify(aptekaRepository, times(1)).save(any(Apteka.class));
        }
    }
}