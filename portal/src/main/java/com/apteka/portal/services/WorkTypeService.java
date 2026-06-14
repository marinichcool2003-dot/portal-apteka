package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.WorkTypeSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;
    private final WorkTypeSecurityService workTypeSecurityService;
    private final CacheManager cacheManager;
    private final GroupTaskRepository groupTaskRepository;
    private final TypeNameValidator typeNameValidator;
    private final SseController sseController;

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

    @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#result.taskGroup().id()")
    @Transactional
    public WorkTypeResponseDTO create(WorkTypeRequestDTO dto, AppUserDetails currentUser) {
        GroupTask groupTask = groupTaskRepository.findById(dto.groupTaskId())
                .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));

        workTypeSecurityService.validateBossOrAdminInGroup(currentUser, groupTask.getUserGroup());

        if (!StringUtils.hasText(dto.name()))
            throw new InvalidWorkTypeNameException();
        String cleanWorkTypeName = typeNameValidator.getCleanName(dto.name());
        validateWorkTypeName(cleanWorkTypeName, dto.groupTaskId());

        WorkType newWorkType = workTypeRepository.save(WorkType.builder()
                .name(cleanWorkTypeName)
                .groupTask(groupTask)
                .build());

        var signal = new SseEventNames.WorkTypeSignalDTO(newWorkType.getGroupTask().getId(), SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);

        return WorkTypeResponseDTO.from(newWorkType);
    }

    @Transactional
    public WorkTypeResponseDTO update(Integer id, WorkTypeRequestDTO dto, AppUserDetails currentUser) {

        WorkType upWorkType = workTypeRepository.findById(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanUpdateOrDelete(currentUser);
        boolean hasChanged = false;

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upWorkType.getName())) {
                validateWorkTypeName(cleanName, upWorkType.getGroupTask().getId());
                upWorkType.setName(cleanName);
                hasChanged = true;
            }
        }

        if (dto.groupTaskId() != null && dto.groupTaskId() > 0) {
            GroupTask newGroupTask = groupTaskRepository.findById(dto.groupTaskId())
                    .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));
            if (!Objects.equals(newGroupTask.getId(), upWorkType.getGroupTask().getId())) {
                validateWorkTypeName(upWorkType.getName(), newGroupTask.getId());
                Integer oldGroupTaskId = upWorkType.getGroupTask().getId();
                cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(oldGroupTaskId);
                upWorkType.setGroupTask(newGroupTask);
                hasChanged = true;
            }
        }

        WorkTypeResponseDTO response = WorkTypeResponseDTO.from(upWorkType);

        if (hasChanged) {
            var cache = cacheManager.getCache(CacheNames.WORK_TYPE);
            if (cache != null) {
                cache.put(id, response);
            }
            cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(response.taskGroup().id());
            var signal = new SseEventNames.EntityUpdateSignalDTO(upWorkType.getId(),
                    SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
        }

        return response;
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        WorkType workType = workTypeRepository.findById(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanUpdateOrDelete(currentUser);

        workTypeRepository.delete(workType);

        cacheManager.getCache(CacheNames.WORK_TYPE).evict(id);
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(workType.getGroupTask().getId());

        var signal = new SseEventNames.WorkTypeSignalDTO(workType.getId(), SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
    }

    private void validateWorkTypeName(String name, Integer groupTaskId) {
        boolean exists = workTypeRepository.existsByNameAndGroupTaskId(name, groupTaskId);
        if (exists) {
            throw new DublicateWorkTypeNameException(name);
        }
    }
}
