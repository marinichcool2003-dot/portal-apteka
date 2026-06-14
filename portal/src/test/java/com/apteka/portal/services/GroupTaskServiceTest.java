package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.components.servicesecurity.GroupTaskSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

@ExtendWith(MockitoExtension.class)
public class GroupTaskServiceTest {
    @Mock
    private GroupTaskRepository groupTaskRepository;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private TypeNameValidator typeNameValidator;
    @Mock
    private GroupTaskSecurityService groupTaskSecurityService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;
    @Mock
	private SseController sseController;

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

        when(userGroupRepository.findById(dto.userGroupId())).thenReturn(Optional.of(userGroup));
        when(typeNameValidator.getCleanName(dto.name())).thenReturn("Накладные");
        when(groupTaskRepository.existsByNameAndUserGroupId("Накладные", dto.userGroupId()))
                .thenReturn(false);
        when(groupTaskRepository.save(any(GroupTask.class))).thenReturn(savedGroupTask);

        GroupTaskResponseDTO result = groupTaskService.create(dto, currentUser);

        assertNotNull(result);
        assertEquals("Накладные", result.name());

        verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, userGroup);
        verify(groupTaskRepository).save(any(GroupTask.class));

    }

    @Test
    void update_ShouldReturnSavedGroupTask_WhenDataIsValid() {
        Integer groupTaskId = 1;
        AppUserDetails currentUser = TestData.mockJustAdmin();
        GroupTask oldGroupTask = TestData.defaultGroupTask();
        GroupTaskRequestDTO dto = new GroupTaskRequestDTO("Алгоритм", null); // Без смены группы

        GroupTask savedGroupTask = TestData.newGroupTask();
        savedGroupTask.setId(groupTaskId);

        when(groupTaskRepository.findById(groupTaskId)).thenReturn(Optional.of(oldGroupTask));
        when(typeNameValidator.getCleanName(dto.name())).thenReturn("Алгоритм");
        when(groupTaskRepository.existsByNameAndUserGroupId("Алгоритм", oldGroupTask.getUserGroup().getId()))
                .thenReturn(false);

        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache(CacheNames.GROUP_TASK)).thenReturn(mockCache);
        when(cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP)).thenReturn(mockCache);

        GroupTaskResponseDTO result = groupTaskService.update(groupTaskId, dto, currentUser);

        assertNotNull(result);
        assertEquals("Алгоритм", result.name());

        verify(groupTaskRepository).findById(groupTaskId);
        verify(groupTaskSecurityService).validateCanUpdateOrDelete(currentUser);
        verify(groupTaskRepository).existsByNameAndUserGroupId("Алгоритм", oldGroupTask.getUserGroup().getId());
    }

    @Test
    void delete_Successful() {
        Integer id = 1;
        GroupTask groupToDelete = TestData.defaultGroupTask();
        AppUserDetails currentUser = TestData.mockJustAdmin();
        Cache mockCache = mock(Cache.class);

        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
        when(groupTaskRepository.findById(id)).thenReturn(Optional.of(groupToDelete));

        groupTaskService.delete(id, currentUser);

        verify(groupTaskRepository).deleteById(id);
        verify(mockCache, atLeastOnce()).evict(any());
    }

    @Test
    void delete_ThrowsException_WhenNotAdmin() {
        Integer id = 1;
        GroupTask groupToDelete = TestData.defaultGroupTask();
        AppUserDetails boss = TestData.mockJustBoss();

        when(groupTaskRepository.findById(id)).thenReturn(Optional.of(groupToDelete));
        doThrow(new AccessDeniedException("Изменять и удалять тип работ может только администратор!"))
                .when(groupTaskSecurityService).validateCanUpdateOrDelete(boss);

        assertThrows(AccessDeniedException.class,
                () -> groupTaskService.delete(id, boss));

        verify(groupTaskRepository, never()).deleteById(any());
    }
}
