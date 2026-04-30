package com.apteka.portal.services;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
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

    public List<WorkType> getAll() {
        return workTypeRepository.findAll();
    }

    @Cacheable(value = "workTypes", key = "#id")
    @Transactional(readOnly = true)
    public WorkType getOne(Integer id) {
        return workTypeRepository.findById(id)
            .orElseThrow(() -> new WorkTaskNotFoundException(id));
    }

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

    @CacheEvict(value = "workTypes", key = "#id")
    @Transactional
    public void delete(Integer id) {
        if (!workTypeRepository.existsById(id)) {
            throw new WorkTaskNotFoundException(id);
        }
        workTypeRepository.deleteById(id);
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
