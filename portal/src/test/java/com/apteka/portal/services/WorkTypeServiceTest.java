package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

import com.apteka.portal.components.servicesecurity.WorkTypeSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

@ExtendWith(MockitoExtension.class)
public class WorkTypeServiceTest {
    @Mock
    private WorkTypeRepository workTypeRepository;
    @Mock
    private GroupTaskRepository groupTaskRepository;
    @Mock
    private WorkTypeSecurityService workTypeSecurityService;
    @Mock
    private TypeNameValidator typeNameValidator;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private SseController sseController;
    @Mock
    Cache cache;
    @InjectMocks
    private WorkTypeService workTypeService;

    private WorkTypeRequestDTO createDto(String name, Integer groupTaskId) {
        return new WorkTypeRequestDTO(name, groupTaskId);
    }

    @Test
    void create_Succesful() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask groupTask = TestData.defaultGroupTask();
        WorkTypeRequestDTO dto = createDto("Удаление накладной", 1);
        WorkType savedWorkType = TestData.defaultWorkType();

        when(groupTaskRepository.findById(dto.groupTaskId())).thenReturn(Optional.of(groupTask));
        when(typeNameValidator.getCleanName(dto.name())).thenReturn("Удаление накладной");
        when(workTypeRepository.existsByNameAndGroupTaskId(dto.name(), dto.groupTaskId())).thenReturn(false);
        when(workTypeRepository.save(any(WorkType.class))).thenReturn(savedWorkType);

        WorkTypeResponseDTO result = workTypeService.create(dto, currentUser);

        assertNotNull(result);
        assertEquals(result.name(), savedWorkType.getName());

        verify(workTypeSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        verify(workTypeRepository).save(any(WorkType.class));

    }

    @Test
    void create_DublicateException() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask groupTask = TestData.defaultGroupTask();
        WorkTypeRequestDTO dto = createDto("Удаление накладной", 1);

        when(groupTaskRepository.findById(dto.groupTaskId())).thenReturn(Optional.of(groupTask));
        when(typeNameValidator.getCleanName(dto.name())).thenReturn("Удаление накладной");
        when(workTypeRepository.existsByNameAndGroupTaskId(dto.name(), dto.groupTaskId())).thenReturn(true);

        assertThrows(DublicateWorkTypeNameException.class, () -> {
            workTypeService.create(dto, currentUser);
        });

        verify(workTypeSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        verify(workTypeRepository, never()).save(any());

    }

    @Test
    void update_Successful() {
        AppUserDetails currentUser = TestData.mockJustAdmin();
        WorkTypeRequestDTO dto = createDto("Маркировка", null);
        WorkType oldWorkType = TestData.defaultWorkType();

        WorkType updatedWorkType = WorkType.builder()
                .id(oldWorkType.getId())
                .name("Маркировка")
                .groupTask(oldWorkType.getGroupTask())
                .build();

        Integer workTypeId = 1;

        Cache mockWorkTypeCache = mock(Cache.class);
        Cache mockWorkTypesByGroupCache = mock(Cache.class);
        when(cacheManager.getCache(CacheNames.WORK_TYPE)).thenReturn(mockWorkTypeCache);
        when(cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP)).thenReturn(mockWorkTypesByGroupCache);

        when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(oldWorkType));
        when(typeNameValidator.getCleanName(dto.name())).thenReturn("Маркировка");
        when(workTypeRepository.existsByNameAndGroupTaskId("Маркировка", oldWorkType.getGroupTask().getId()))
                .thenReturn(false);

        WorkTypeResponseDTO result = workTypeService.update(workTypeId, dto, currentUser);

        assertNotNull(result);
        assertEquals("Маркировка", result.name());

        verify(workTypeSecurityService).validateCanUpdateOrDelete(currentUser);
        verify(mockWorkTypeCache).put(eq(workTypeId), any(WorkTypeResponseDTO.class));
        verify(mockWorkTypesByGroupCache).evict(updatedWorkType.getGroupTask().getId());
    }

    @Test
    void delete_Successful() {
        AppUserDetails currentUser = TestData.mockJustAdmin();
        WorkType workTypeToDelete = TestData.defaultWorkType();

        Integer workTypeId = 1;

        Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
        when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(workTypeToDelete));

        workTypeService.delete(workTypeId, currentUser);

        verify(workTypeSecurityService).validateCanUpdateOrDelete(currentUser);
        verify(workTypeRepository).delete(workTypeToDelete);
        verify(mockCache, times(2)).evict(any());
    }

}
