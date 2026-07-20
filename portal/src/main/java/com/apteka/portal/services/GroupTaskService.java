package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.apteka.portal.components.servicesecurity.GroupTaskSecurityService;
import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.grouptask.GroupTaskRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.exceptions.DuplicateGroupTaskException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
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
    private final IsActiveValidator isActiveValidator;
    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<GroupTaskResponseDTO> getByGroups(Integer creatorGroupId, Integer intendedGroupId, Boolean isActive,
            AppUserDetails currentUser) {

        String cacheKey = creatorGroupId + ":" + intendedGroupId;
        Cache cache = cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP);

        if (Boolean.TRUE.equals(isActive) && cache != null) {
            @SuppressWarnings("unchecked")
            List<GroupTaskResponseDTO> cachedList = cache.get(cacheKey, List.class);
            if (cachedList != null) {
                return cachedList;
            }
        }

        if (!userGroupRepository.existsById(creatorGroupId)) {
            throw new GroupTaskNotFoundException(creatorGroupId);
        }

        if (!userGroupRepository.existsById(intendedGroupId)) {
            throw new GroupUserNotFoundException("Группа не найдена!");
        }

        List<GroupTask> tasks = groupTaskRepository.findByGroupsAndIsActive(creatorGroupId, intendedGroupId, isActive);

        boolean allTasksAreTrulyActive = tasks.stream().allMatch(isActiveValidator::isGroupTaskActive);

        List<GroupTaskResponseDTO> response = tasks.stream()
                .map(GroupTaskResponseDTO::from)
                .toList();

        if (Boolean.TRUE.equals(isActive) && allTasksAreTrulyActive && cache != null) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public GroupTaskResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        Cache cache = cacheManager.getCache(CacheNames.GROUP_TASK);
        if (cache != null) {
            GroupTaskResponseDTO cachedDto = cache.get(id, GroupTaskResponseDTO.class);
            if (cachedDto != null) {
                return cachedDto;
            }
        }
        GroupTask groupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));

        boolean isActive = isActiveValidator.isGroupTaskActive(groupTask);

        if (!isActive) {
            groupTaskSecurityService.validateCanWorkGroupTask(currentUser, groupTask.getCreatorGroup());
        }

        GroupTaskResponseDTO response = GroupTaskResponseDTO.from(groupTask);

        if (isActive && cache != null) {
            cache.put(id, response);
        }

        return response;
    }

    @Transactional
    public GroupTaskResponseDTO create(GroupTaskRequestDTO dto, AppUserDetails currentUser) {

        groupTaskSecurityService.validateGroupVisibility(dto.creatorGroupId(), dto.intendedGroupId());

        UserGroup creatorGroup = userGroupRepository.findById(dto.creatorGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.creatorGroupId()));

        UserGroup intendedGroup = userGroupRepository.findById(dto.intendedGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.intendedGroupId()));

        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, creatorGroup);

        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateGroupTaskName(cleanName, creatorGroup.getId(), intendedGroup.getId());

        GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                .name(cleanName)
                .creatorGroup(creatorGroup)
                .intendedGroup(intendedGroup)
                .isActive(true)
                .build());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.GroupTaskSignalDTO(creatorGroup.getId(), intendedGroup.getId(),
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }

        return GroupTaskResponseDTO.from(saved);
    }

    @Transactional
    public GroupTaskResponseDTO update(Integer id, GroupTaskUpdateRequestDTO dto, AppUserDetails currentUser,
            Boolean confirm) {
        GroupTask upGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        String oldCacheKey = upGroup.getCreatorGroup().getId() + ":" + upGroup.getIntendedGroup().getId();

        boolean nameChange = false;
        boolean groupsChange = false;

        if (dto.creatorGroupId() != null && !Objects.equals(dto.creatorGroupId(), upGroup.getCreatorGroup().getId())) {
            UserGroup creatorGroup = userGroupRepository.findById(dto.creatorGroupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.creatorGroupId()));
            upGroup.setCreatorGroup(creatorGroup);
            groupsChange = true;
        }

        if (dto.intendedGroupId() != null
                && !Objects.equals(dto.intendedGroupId(), upGroup.getIntendedGroup().getId())) {
            UserGroup intendedGroup = userGroupRepository.findById(dto.intendedGroupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.intendedGroupId()));
            upGroup.setIntendedGroup(intendedGroup);
            groupsChange = true;
        }

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upGroup.getName())) {
                validateGroupTaskName(cleanName, upGroup.getCreatorGroup().getId(), upGroup.getIntendedGroup().getId());
                upGroup.setName(cleanName);
                nameChange = true;
            }
        }

        boolean hasChange = nameChange || groupsChange;

        if (hasChange) {
            boolean isConfirmed = Boolean.TRUE.equals(confirm);
            groupTaskSecurityService.validateGroupTaskUpdate(currentUser, upGroup, nameChange, groupsChange,
                    isConfirmed);
            upGroup.setUpdatedBy(currentUser.getDisplayName());

            upGroup = groupTaskRepository.save(upGroup);
        }

        GroupTaskResponseDTO response = GroupTaskResponseDTO.from(upGroup);

        if (hasChange && TransactionSynchronizationManager.isSynchronizationActive()) {
            final GroupTaskResponseDTO finalResponse = response;
            final String newCacheKey = upGroup.getCreatorGroup().getId() + ":" + upGroup.getIntendedGroup().getId();
            final Integer groupTaskId = upGroup.getId();
            final String oldKey = oldCacheKey;

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var cacheByGroup = cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP);
                    if (cacheByGroup != null) {
                        cacheByGroup.evict(oldKey);
                        if (!oldKey.equals(newCacheKey)) {
                            cacheByGroup.evict(newCacheKey);
                        }
                    }

                    var cache = cacheManager.getCache(CacheNames.GROUP_TASK);
                    if (cache != null) {
                        cache.put(groupTaskId, finalResponse);
                    }

                    var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }

        return response;
    }

    @Transactional
    public void safeDelete(Integer id, AppUserDetails currentUser) {
        GroupTask deletedGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, deletedGroup.getCreatorGroup());
        if (isActiveValidator.isGroupTaskActive(deletedGroup)) {
            deletedGroup.setActive(false);
            Integer groupTaskId = deletedGroup.getId();
            String cacheKey = deletedGroup.getCreatorGroup().getId() + ":" + deletedGroup.getIntendedGroup().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var groupTaskCache = cacheManager.getCache(CacheNames.GROUP_TASK);
                        if (groupTaskCache != null) {
                            groupTaskCache.evict(groupTaskId);
                        }

                        var groupTasksByGroupCache = cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP);
                        if (groupTasksByGroupCache != null) {
                            groupTasksByGroupCache.evict(cacheKey);
                        }

                        var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void restore(Integer id, AppUserDetails currentUser) {
        GroupTask restoredGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, restoredGroup.getCreatorGroup());

        if (!isActiveValidator.isGroupTaskActive(restoredGroup)) {
            restoredGroup.setActive(true);
            Integer groupTaskId = restoredGroup.getId();
            String cacheKey = restoredGroup.getCreatorGroup().getId() + ":" + restoredGroup.getIntendedGroup().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var groupTasksByGroupCache = cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP);
                        if (groupTasksByGroupCache != null) {
                            groupTasksByGroupCache.evict(cacheKey);
                        }

                        var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm) {
        GroupTask deletedGroupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanPermanentDelete(deletedGroupTask, confirm);
        groupTaskRepository.deleteById(id);

        String cachekey = deletedGroupTask.getCreatorGroup().getId().toString() + ':' +
                deletedGroupTask.getIntendedGroup().getId().toString();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var groupTaskCache = cacheManager.getCache(CacheNames.GROUP_TASK);
                    if (groupTaskCache != null) {
                        groupTaskCache.evict(id);
                    }

                    var groupTasksByGroupCache = cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP);
                    if (groupTasksByGroupCache != null) {
                        groupTasksByGroupCache.evict(cachekey);
                    }

                    var workTypesByGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                    if (workTypesByGroupCache != null) {
                        workTypesByGroupCache.evict(id);
                    }

                    var signal = new SseEventNames.GroupTaskSignalDTO(deletedGroupTask.getCreatorGroup().getId(),
                            deletedGroupTask.getIntendedGroup().getId(),
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }
    }

    private void validateGroupTaskName(String cleanName, Integer creatorGroupId, Integer intendedGroupId) {
        if (groupTaskRepository.existsByNameAndCreatorGroupIdAndIntendedGroupId(cleanName, creatorGroupId,
                intendedGroupId)) {
            throw new DuplicateGroupTaskException(cleanName);
        }
    }
}
