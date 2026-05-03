package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apteka.portal.components.UserGroupSecurityService;
import com.apteka.portal.dtos.request.UserGroupRequestDTO;
import com.apteka.portal.exceptions.DublicateGroupUserException;
import com.apteka.portal.models.AppUserDetails; 
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceTest {

    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private UserGroupSecurityService userGroupSecurityService;
    @InjectMocks
    private UserGroupService userGroupService;

    // Вспомогательный метод для создания DTO
    private UserGroupRequestDTO createDto(String name) {
        return new UserGroupRequestDTO(name, "+79991112233");
    }

    @Test
    void create_Successful() {
        // Arrange
        UserGroupRequestDTO dto = createDto("Розница");
        UserGroup savedGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            when(userGroupRepository.findByName("Розница")).thenReturn(Optional.empty());
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(savedGroup);

            // Act
            UserGroup result = userGroupService.create(dto);

            // Assert
            assertNotNull(result);
            assertEquals("Розница", result.getName());

            // Проверяем вызовы зависимостей
            verify(userGroupSecurityService).checkCanCreateGroup(currentUser);
            verify(userGroupRepository).save(any(UserGroup.class));
        }
    }

    @Test
    void update_ShouldThrowException_WhenNameIsTakenByAnotherGroup() {
        // Arrange
        Integer existingId = 1;
        UserGroupRequestDTO dto = createDto("Новое наименование группы");

        UserGroup existingInDb = TestData.defaulUserGroup();
        UserGroup anotherGroup = TestData.newDefaulUserGroup();

        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            // Находим редактируемую группу
            when(userGroupRepository.findById(existingId)).thenReturn(Optional.of(existingInDb));
            // Имитируем, что имя занято другой группой (ID не совпадают)
            when(userGroupRepository.findByName("Новое наименование группы")).thenReturn(Optional.of(anotherGroup));

            // Act & Assert
            assertThrows(DublicateGroupUserException.class, () -> userGroupService.update(existingId, dto));
        }
    }

    @Test
    void delete_ShouldCallRepository_WhenUserHasAccess() {
        // Arrange
        Integer id = 1;
        UserGroup groupToDelete = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            when(userGroupRepository.findById(id)).thenReturn(Optional.of(groupToDelete));

            // Act
            userGroupService.delete(id);

            // Assert
            verify(userGroupSecurityService).checkCanCreateGroup(currentUser);
            verify(userGroupRepository).delete(groupToDelete);
        }
    }
}
