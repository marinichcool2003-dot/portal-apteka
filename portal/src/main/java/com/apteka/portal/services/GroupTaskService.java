package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.components.TypeNameValidator;
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTaskService {

    private final GroupTaskRepository groupTaskRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupTaskSecurityService groupTaskSecurityService;
    private final CacheManager cacheManager;
    private final TypeNameValidator typeNameValidator;

    @Cacheable(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#userGroupId", sync = true)
    @Transactional(readOnly = true)
    public List<GroupTaskResponseDTO> getByUserGroup(Integer userGroupId) {
        if (userGroupRepository.existsById(userGroupId)) {
            return groupTaskRepository.findByUserGroupId(userGroupId).stream()
                    .map(GroupTaskResponseDTO::from).toList();
        }
        throw new GroupUserNotFoundException(userGroupId);
    }

    @Cacheable(value = CacheNames.GROUP_TASK, key = "#id", sync = true)
    @Transactional(readOnly = true)
    public GroupTaskResponseDTO getOne(Integer id) {
        GroupTask groupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        return GroupTaskResponseDTO.from(groupTask);
    }

    @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#dto.userGroupId()")
    @Transactional
    public GroupTaskResponseDTO create(GroupTaskRequestDTO dto, AppUserDetails currentUser) {
        UserGroup userGroup = userGroupRepository.findById(dto.userGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.userGroupId()));
        String cleanName = typeNameValidator.getCleanName(dto.name());

        validateGroupTaskName(cleanName, dto.userGroupId());
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, userGroup);

        GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                .name(cleanName)
                .userGroup(userGroup)
                .build());

        return GroupTaskResponseDTO.from(saved);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.GROUP_TASK, key = "#id"),
            @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#result.userGroup().id()")
    })
    @Transactional
    public GroupTaskResponseDTO update(Integer id, GroupTaskRequestDTO dto, AppUserDetails currentUser) {
        GroupTask upGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, upGroup.getUserGroup());

        String cleanName = typeNameValidator.getCleanName(dto.name());

        if (Objects.equals(cleanName, upGroup.getName())) {
            return GroupTaskResponseDTO.from(upGroup);
        }

        validateGroupTaskName(cleanName, upGroup.getUserGroup().getId());
        upGroup.setName(validateGroupTaskName(cleanName, id));

        GroupTask saved = groupTaskRepository.save(upGroup);

        return GroupTaskResponseDTO.from(saved);
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        GroupTask deletedGroupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, deletedGroupTask.getUserGroup());
        groupTaskRepository.deleteById(id);

        cacheManager.getCache(CacheNames.GROUP_TASK).evict(id);
        cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP).evict(deletedGroupTask.getUserGroup().getId());
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(id);
    }

    private String validateGroupTaskName(String cleanName, Integer userGroupId) {
        if (cleanName == null || cleanName.isBlank()) {
            throw new InvalidGroupTaskException(cleanName);
        }
        if (groupTaskRepository.existsByNameAndUserGroupId(cleanName, userGroupId)) {
            throw new DublicateGroupTaskException(cleanName);
        }
        
        return cleanName;
    }
}
