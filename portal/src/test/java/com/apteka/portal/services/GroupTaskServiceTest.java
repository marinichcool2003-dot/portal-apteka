package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.GroupTaskRepository;

@ExtendWith(MockitoExtension.class)
public class GroupTaskServiceTest {
    @Mock
    private GroupTaskRepository groupTaskRepository;
    @Mock
    private UserGroupService userGroupService;
    @Mock
    private GroupTaskSecurityService groupTaskSecurityService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    @InjectMocks
    private GroupTaskService groupTaskService;

    private GroupTaskRequestDTO createDto(String name, Integer userGroupId) {
        return new GroupTaskRequestDTO(name, userGroupId);
    }

    @Test
    void create_ShouldReturnSavedGroupTask_WhenDataIsValid() {

        GroupTaskRequestDTO dto = createDto("Накладные", 1);
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.defaulUserGroup();
        GroupTask savedGroupTask = TestData.defaultGroupTask();

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);

            when(userGroupService.getOne(dto.userGroupId())).thenReturn(userGroup);
            when(groupTaskRepository.findByNameAndUserGroupId("Накладные", dto.userGroupId()))
                    .thenReturn(Optional.empty());
            when(groupTaskRepository.save(any(GroupTask.class))).thenReturn(savedGroupTask);

            GroupTask result = groupTaskService.create(dto);

            assertNotNull(result);
            assertEquals("Накладные", result.getName());

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, userGroup);
            verify(groupTaskRepository).save(any(GroupTask.class));
        }
    }

    @Test
    void update_SholdReturnSavedGroupTask_WhenDataIsValid() {
        Integer groupTaskId = 1;
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask oldGroupTask = TestData.defaultGroupTask();
        GroupTaskRequestDTO dto = createDto("Алгоритм", groupTaskId);
        UserGroup userGroup = TestData.defaulUserGroup();

        GroupTask savedGroupTask = TestData.newGroupTask();
        savedGroupTask.setId(groupTaskId);

        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(groupTaskRepository.findById(groupTaskId)).thenReturn(Optional.of(oldGroupTask));
            when(groupTaskRepository.findByNameAndUserGroupId("Алгоритм", groupTaskId)).thenReturn(Optional.empty());
            when(groupTaskRepository.save(any(GroupTask.class))).thenReturn(savedGroupTask);

            GroupTask result = groupTaskService.update(groupTaskId, dto);

            assertNotNull(result);
            assertEquals("Алгоритм", result.getName());

            verify(groupTaskRepository).findById(groupTaskId);
            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, userGroup);
            verify(groupTaskRepository).save(any(GroupTask.class));
        }
    }

    @Test
    void delete_Successful() {
        Integer id = 1;
        GroupTask groupToDelete = TestData.defaultGroupTask();
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.defaulUserGroup();
        Cache mockCache = mock(Cache.class);

        try(MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)){
            mockedSecurity.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(cacheManager.getCache(anyString())).thenReturn(mockCache);
            when(groupTaskRepository.findById(id)).thenReturn(Optional.of(groupToDelete));

            groupTaskService.delete(id);

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, userGroup);
            verify(groupTaskRepository).deleteById(id);
            verify(mockCache, atLeastOnce()).evict(any());
        }
    }
}
