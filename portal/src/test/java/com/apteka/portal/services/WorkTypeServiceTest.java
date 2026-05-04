package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.WorkTypeRepository;

@ExtendWith(MockitoExtension.class)
public class WorkTypeServiceTest {
    @Mock private WorkTypeRepository workTypeRepository;
    @Mock private GroupTaskService groupTaskService;
    @Mock private GroupTaskSecurityService groupTaskSecurityService;
    @Mock private CacheManager cacheManager;
    @Mock Cache cache;
    @InjectMocks private WorkTypeService workTypeService;

    private WorkTypeRequestDTO createDto(String name, Integer groupTaskId) {
        return new WorkTypeRequestDTO(name, groupTaskId);
    } 

    @Test
    void create_Succesful(){
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask groupTask = TestData.defaultGroupTask();
        WorkTypeRequestDTO dto = createDto("Удаление накладной", 1);
        WorkType savedWorkType = TestData.defaultWorkType();

        try(MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(groupTaskService.getOne(dto.groupTaskId())).thenReturn(groupTask);
            when(workTypeRepository.existsByNameAndGroupTaskId(dto.name(), dto.groupTaskId())).thenReturn(false);
            when(workTypeRepository.save(any(WorkType.class))).thenReturn(savedWorkType);

            WorkType result = workTypeService.create(dto);

            assertNotNull(result);
            assertEquals(result.getName(), savedWorkType.getName());

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
            verify(workTypeRepository).save(any(WorkType.class));
        }
    }
    
    @Test 
    void create_DublicateException() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask groupTask = TestData.defaultGroupTask();
        WorkTypeRequestDTO dto = createDto("Удаление накладной", 1);

        try(MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(groupTaskService.getOne(dto.groupTaskId())).thenReturn(groupTask);
            when(workTypeRepository.existsByNameAndGroupTaskId(dto.name(), dto.groupTaskId())).thenReturn(true);

            assertThrows(DublicateWorkTypeNameException.class, () -> {
                workTypeService.create(dto);
            });

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
            verify(workTypeRepository, never()).save(any());
        }
    }

    @Test
    void update_Succesful() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        GroupTask groupTask = TestData.defaultGroupTask();
        WorkTypeRequestDTO dto = createDto("Маркировка", 1);
        WorkType oldWorkType = TestData.defaultWorkType();
        WorkType newWorkType = TestData.newDefaultWorkType();

        Integer workTypeId = 1;

        try(MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(oldWorkType));
            when(groupTaskService.getOne(dto.groupTaskId())).thenReturn(groupTask);
            when(groupTaskService.getOne(dto.groupTaskId())).thenReturn(groupTask);
            when(workTypeRepository.save(any(WorkType.class))).thenReturn(newWorkType);

            WorkType result = workTypeService.update(workTypeId, dto);

            assertNotNull(result);
            assertEquals(newWorkType.getName(), result.getName());

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
            verify(workTypeRepository).save(any(WorkType.class));
        }
    }

    @Test
    void delete_Succesful() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        WorkType workTypeToDelete = TestData.defaultWorkType();
        GroupTask groupTask = workTypeToDelete.getGroupTask();

        Integer workTypeId = 1;

        try(MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
            mockedStatic.when(SecurityUtils::getRequiredCurrentUser).thenReturn(currentUser);
            when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(workTypeToDelete));
            when(cacheManager.getCache(anyString())).thenReturn(cache);
            
            workTypeService.delete(workTypeId);

            verify(groupTaskSecurityService).validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
            verify(workTypeRepository).delete(workTypeToDelete);
            verify(cache, times(2)).evict(any());
        }
    }
}
