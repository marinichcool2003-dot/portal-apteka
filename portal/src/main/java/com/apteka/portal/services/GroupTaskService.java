package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.servicesecurity.GroupTaskSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
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

    private final SseController sseController;

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

    @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#result.userGroup().id()")
    @Transactional
    public GroupTaskResponseDTO create(GroupTaskRequestDTO dto, AppUserDetails currentUser) {
        UserGroup userGroup = userGroupRepository.findById(dto.userGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.userGroupId()));

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, userGroup);

        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateGroupTaskName(cleanName, dto.userGroupId());

        GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                .name(cleanName)
                .userGroup(userGroup)
                .build());

        var signal = new SseEventNames.GroupTaskSignalDTO(dto.userGroupId(), SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);

        return GroupTaskResponseDTO.from(saved);
    }

    @Transactional
    public GroupTaskResponseDTO update(Integer id, GroupTaskRequestDTO dto, AppUserDetails currentUser) {

        GroupTask upGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanUpdateOrDelete(currentUser);

        boolean hasChange = false;

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upGroup.getName())) {
                validateGroupTaskName(cleanName, upGroup.getUserGroup().getId());
                upGroup.setName(cleanName);
                hasChange = true;
            }
        }

        if (dto.userGroupId() != null && dto.userGroupId() > 0) {
            if (!currentUser.hasRole(UserRole.ADMIN)) {
                throw new AccessDeniedException("Только администратор может переместить тип задачи на другую группу!");
            }
            UserGroup userGroup = userGroupRepository.findById(dto.userGroupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.userGroupId()));
            cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP).evict(upGroup.getUserGroup().getId());
            upGroup.setUserGroup(userGroup);
            hasChange = true;
        }

        GroupTaskResponseDTO response = GroupTaskResponseDTO.from(upGroup);

        if (hasChange) {
            var cache = cacheManager.getCache(CacheNames.GROUP_TASK);
            if (cache != null) {
                cache.put(id, response);
            }
            cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP).evict(response.userGroup().id());
            var signal = new SseEventNames.EntityUpdateSignalDTO(upGroup.getId(), SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
        }

        return response;
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        GroupTask deletedGroupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanUpdateOrDelete(currentUser);
        groupTaskRepository.deleteById(id);

        cacheManager.getCache(CacheNames.GROUP_TASK).evict(id);
        cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP).evict(deletedGroupTask.getUserGroup().getId());
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(id);

        var signal = new SseEventNames.GroupTaskSignalDTO(deletedGroupTask.getUserGroup().getId(),
                SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
    }

    private void validateGroupTaskName(String cleanName, Integer userGroupId) {
        if (groupTaskRepository.existsByNameAndUserGroupId(cleanName, userGroupId)) {
            throw new DublicateGroupTaskException(cleanName);
        }
    }
}
