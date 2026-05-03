package com.apteka.portal.services;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;
    private final GroupTaskService groupTaskService;
    private final GroupTaskSecurityService groupTaskSecurityService;
    private final CacheManager cacheManager;

    @Cacheable(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#groupTaskId")
    public List<WorkType> getByGroupTask(Integer groupTaskId) {
        groupTaskService.getOne(groupTaskId);
        return workTypeRepository.findByGroupTask(groupTaskId);
    }

    @Cacheable(value = CacheNames.WORK_TYPE, key = "#id")
    @Transactional(readOnly = true)
    public WorkType getOne(Integer id) {
        return workTypeRepository.findById(id)
            .orElseThrow(() -> new WorkTaskNotFoundException(id));
    }

    @Caching(evict = {
        @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#dto.groupTaskId()")
    })
    @Transactional
    public WorkType create(WorkTypeRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask groupTask = groupTaskService.getOne(dto.groupTaskId());
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        validateWorkTypeName(dto);
        WorkType newWorkType = WorkType.builder()
            .name(dto.name().strip())
            .groupTask(groupTask).build();
        return workTypeRepository.save(newWorkType);
    }

    @Caching(evict = {
        @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#dto.groupTaskId()")
    }, put = {
        @CachePut(value = CacheNames.WORK_TYPE, key = "#id")
    })
    @Transactional
    public WorkType update(Integer id, WorkTypeRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask groupTask = groupTaskService.getOne(dto.groupTaskId());
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        WorkType upWorkType = getOne(id);
        validateWorkTypeName(dto);
        upWorkType.setName(dto.name());
        return workTypeRepository.save(upWorkType);
    }

    @Transactional
    public void delete(Integer id) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        WorkType workType = getOne(id);

        GroupTask groupTask = workType.getGroupTask();
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        workTypeRepository.delete(workType);

        cacheManager.getCache(CacheNames.WORK_TYPE).evict(id);
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(groupTask.getId());
    }

    private void validateWorkTypeName(WorkTypeRequestDTO dto) {
        if (dto.name() == null || dto.name().isBlank()) {
            throw new InvalidWorkTypeNameException();
        }
        boolean exists = workTypeRepository.existsByNameAndGroupTaskId(dto.name(), dto.groupTaskId());
        if (exists) {
            throw new DublicateWorkTypeNameException(dto.name());
        }
    }
}
