package com.apteka.portal.services;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;
    private final GroupTaskSecurityService groupTaskSecurityService;
    private final CacheManager cacheManager;
    private final GroupTaskRepository groupTaskRepository;

    @Cacheable(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#groupTaskId", sync = true)
    @Transactional(readOnly = true)
    public List<WorkTypeResponseDTO> getByGroupTask(Integer groupTaskId) {
        if (groupTaskRepository.existsById(groupTaskId)) {
            return workTypeRepository.findByGroupTaskId(groupTaskId).stream()
                    .map(WorkTypeResponseDTO::from).toList();
        }
        throw new GroupTaskNotFoundException(groupTaskId);
    }

    @Cacheable(value = CacheNames.WORK_TYPE, key = "#id", sync = true)
    @Transactional(readOnly = true)
    public WorkTypeResponseDTO getOne(Integer id) {
        WorkType workType = workTypeRepository.findById(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        return WorkTypeResponseDTO.from(workType);
    }

    @Transactional
    public WorkTypeResponseDTO create(WorkTypeRequestDTO dto, AppUserDetails currentUser) {
        GroupTask groupTask = groupTaskRepository.findById(dto.groupTaskId())
                .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());
        validateWorkTypeName(dto);

        WorkType newWorkType = workTypeRepository.save(WorkType.builder()
                .name(dto.name().strip())
                .groupTask(groupTask)
                .build());

        evictGroupCache(groupTask.getId());

        return WorkTypeResponseDTO.from(newWorkType);
    }

    @Transactional
    public WorkTypeResponseDTO update(Integer id, WorkTypeRequestDTO dto, AppUserDetails currentUser) {
        WorkType upWorkType = workTypeRepository.findById(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        GroupTask newGroupTask = groupTaskRepository.findById(dto.groupTaskId())
                .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, newGroupTask.getUserGroup());
        validateWorkTypeName(dto);

        Integer oldGroupTaskId = upWorkType.getGroupTask().getId();

        upWorkType.setName(dto.name().strip());
        upWorkType.setGroupTask(newGroupTask);

        WorkType saved = workTypeRepository.save(upWorkType);

        evictWorkTypeCache(id);
        evictGroupCache(oldGroupTaskId);
        if (!oldGroupTaskId.equals(newGroupTask.getId())) {
            evictGroupCache(newGroupTask.getId());
        }

        return WorkTypeResponseDTO.from(saved);
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        WorkType workType = workTypeRepository.findById(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        GroupTask groupTask = workType.getGroupTask();
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());

        Integer groupTaskId = groupTask.getId();
        workTypeRepository.delete(workType);

        evictWorkTypeCache(id);
        evictGroupCache(groupTaskId);

        var workTypesCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
        if (workTypesCache != null)
            workTypesCache.evict(id);
    }

    private void evictWorkTypeCache(Integer id) {
        var cache = cacheManager.getCache(CacheNames.WORK_TYPE);
        if (cache != null)
            cache.evict(id);
    }

    private void evictGroupCache(Integer groupTaskId) {
        var cache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
        if (cache != null)
            cache.evict(groupTaskId);
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
