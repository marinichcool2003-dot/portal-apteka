package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.apteka.portal.components.servicesecurity.GroupTaskSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.grouptask.GroupTaskRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskUpdateRequestDTO;
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

    @Cacheable(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#creatorGroupId.toString() + ':' + #executorGroupId.toString()", condition = "#isActive == true", sync = true)
    @Transactional(readOnly = true)
    public List<GroupTaskResponseDTO> getByGroups(Integer creatorGroupId, Integer executorGroupId, Boolean isActive,
            AppUserDetails currentUser) {
        if (Boolean.FALSE.equals(isActive)) {

        }
        boolean creatorGroupExists = userGroupRepository.existsById(creatorGroupId);
        boolean executorGroupExists = userGroupRepository.existsById(executorGroupId);
        if (!(creatorGroupExists && executorGroupExists)) {
            throw new GroupUserNotFoundException("Группа не найдена!");
        }

        return groupTaskRepository.findByGroupsAndActive(creatorGroupId, executorGroupId, isActive).stream()
                .map(GroupTaskResponseDTO::from).toList();

    }

    @Cacheable(value = CacheNames.GROUP_TASK, key = "#id", sync = true)
    @Transactional(readOnly = true)
    public GroupTaskResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        GroupTask groupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        return GroupTaskResponseDTO.from(groupTask);
    }

    @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#result.creatorGroup.id.toString() + ':' + #result.executorGroup.id.toString()")
    @Transactional
    public GroupTaskResponseDTO create(GroupTaskRequestDTO dto, AppUserDetails currentUser) {

        groupTaskSecurityService.validateGroupVisibility(dto.creatorGroupId(), dto.executorGroupId());

        UserGroup creatorGroup = userGroupRepository.findById(dto.creatorGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.creatorGroupId()));

        UserGroup executorGroup = userGroupRepository.findById(dto.executorGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.executorGroupId()));

        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, creatorGroup);

        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateGroupTaskName(cleanName, creatorGroup.getId(), executorGroup.getId());

        GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                .name(cleanName)
                .creatorGroup(creatorGroup)
                .executorGroup(executorGroup)
                .build());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.GroupTaskSignalDTO(creatorGroup.getId(), executorGroup.getId(),
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
        String oldCacheKey = upGroup.getCreatorGroup().getId() + ":" + upGroup.getExecutorGroup().getId();

        boolean nameChange = false;
        boolean groupsChange = false;

        if (dto.creatorGroupId() != null && !Objects.equals(dto.creatorGroupId(), upGroup.getCreatorGroup().getId())) {
            UserGroup creatorGroup = userGroupRepository.findById(dto.creatorGroupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.creatorGroupId()));
            upGroup.setCreatorGroup(creatorGroup);
            groupsChange = true;
        }

        if (dto.executorGroupId() != null
                && !Objects.equals(dto.executorGroupId(), upGroup.getExecutorGroup().getId())) {
            UserGroup executorGroup = userGroupRepository.findById(dto.executorGroupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.executorGroupId()));
            upGroup.setExecutorGroup(executorGroup);
            groupsChange = true;
        }

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upGroup.getName())) {
                validateGroupTaskName(cleanName, upGroup.getCreatorGroup().getId(), upGroup.getExecutorGroup().getId());
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
            final String newCacheKey = upGroup.getCreatorGroup().getId() + ":" + upGroup.getExecutorGroup().getId();
            final Integer groupTaskId = upGroup.getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var cache = cacheManager.getCache(CacheNames.GROUP_TASK);
                    if (cache != null) {
                        if (!oldCacheKey.equals(newCacheKey)) {
                            cache.evict(oldCacheKey);
                        }
                        cache.put(newCacheKey, finalResponse);
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
        if (isActive(deletedGroup.getCreatorGroup(), deletedGroup.getExecutorGroup(), deletedGroup)) {
            deletedGroup.setActive(false);
            Integer groupTaskId = deletedGroup.getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
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

        if (!isActive(restoredGroup.getCreatorGroup(), restoredGroup.getExecutorGroup(), restoredGroup)) {
            restoredGroup.setActive(true);
            Integer groupTaskId = restoredGroup.getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
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
                deletedGroupTask.getExecutorGroup().getId().toString();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    cacheManager.getCache(CacheNames.GROUP_TASK).evict(id);
                    cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP)
                            .evict(cachekey);
                    cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(id);

                    var signal = new SseEventNames.GroupTaskSignalDTO(deletedGroupTask.getCreatorGroup().getId(),
                            deletedGroupTask.getExecutorGroup().getId(),
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }
    }

    private void validateGroupTaskName(String cleanName, Integer creatorGroupId, Integer executorGroupId) {
        if (groupTaskRepository.existsByNameAndCreatorGroupIdAndExecutorGroupId(cleanName, creatorGroupId,
                executorGroupId)) {
            throw new DublicateGroupTaskException(cleanName);
        }
    }

    private boolean isActive(UserGroup creatorGroup, UserGroup executorGroup, GroupTask groupTask) {
        return creatorGroup.isActive() && executorGroup.isActive() && groupTask.isActive();
    };
}
