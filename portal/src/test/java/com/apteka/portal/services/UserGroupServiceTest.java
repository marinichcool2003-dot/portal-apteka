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
import com.apteka.portal.dtos.response.UserGroupResponseDTO;
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

    private UserGroupRequestDTO createDto(String name) {
        return new UserGroupRequestDTO(name, "+79991112233");
    }

    @Test
    void create_Successful() {
        UserGroupRequestDTO dto = createDto("Розница");
        UserGroup savedGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);

            when(userGroupRepository.findByName("Розница")).thenReturn(Optional.empty());
            when(userGroupRepository.save(any(UserGroup.class))).thenReturn(savedGroup);

            UserGroupResponseDTO result = userGroupService.create(dto);

            assertNotNull(result);
            assertEquals("Розница", result.name());

            verify(userGroupSecurityService).checkCanCreateGroup(currentUser);
            verify(userGroupRepository).save(any(UserGroup.class));
        }
    }

    @Test
    void update_ShouldThrowException_WhenNameIsTakenByAnotherGroup() {
        Integer existingId = 2;
        UserGroupRequestDTO dto = createDto("АХО");

        UserGroup existingInDb = TestData.defaulUserGroup();
        UserGroup anotherGroup = UserGroup.builder().name(dto.name()).build();

        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);

            when(userGroupRepository.findById(existingId)).thenReturn(Optional.of(existingInDb));
            when(userGroupRepository.findByName(dto.name())).thenReturn(Optional.of(anotherGroup));

            assertThrows(DublicateGroupUserException.class, () -> userGroupService.update(existingId, dto));
        }
    }

    @Test
    void delete_ShouldCallRepository_WhenUserHasAccess() {
        Integer id = 1;
        UserGroup groupToDelete = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);

            when(userGroupRepository.findById(id)).thenReturn(Optional.of(groupToDelete));

            userGroupService.delete(id);

            verify(userGroupSecurityService).checkCanCreateGroup(currentUser);
            verify(userGroupRepository).delete(groupToDelete);
        }
    }
}
