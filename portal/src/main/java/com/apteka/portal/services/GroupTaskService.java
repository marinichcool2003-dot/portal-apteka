package com.apteka.portal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.apteka.portal.models.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.cache.SafeCacheService;
import com.apteka.portal.components.servicesecurity.GroupTaskSecurityService;
import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.grouptask.GroupTaskBulkToAptekaRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskRequestDTO;
import com.apteka.portal.dtos.request.grouptask.GroupTaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.GroupTaskBulkToAptekaResponseDTO;
import com.apteka.portal.dtos.response.GroupTaskBulkToAptekaResponseDTO.GroupTaskBulkItemResultDTO;
import com.apteka.portal.dtos.response.GroupTaskResponseDTO;
import com.apteka.portal.exceptions.DuplicateGroupTaskException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.UserGroupRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTaskService {
    private final GroupTaskRepository groupTaskRepository;
    private final UserGroupRepository userGroupRepository;
    private final WorkTypeRepository workTypeRepository;
    private final GroupTaskSecurityService groupTaskSecurityService;
    private final SafeCacheService safeCacheService;
    private final TypeNameValidator typeNameValidator;
    private final IsActiveValidator isActiveValidator;
    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<GroupTaskResponseDTO> getByGroups(Integer creatorGroupId, Integer intendedGroupId, Boolean isActive,
            AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            groupTaskSecurityService.validateGroupVisibility(creatorGroupId, intendedGroupId);
        }
        String cacheKey = creatorGroupId + ":" + intendedGroupId;

        if (Boolean.TRUE.equals(isActive)) {
            var cachedList = safeCacheService.getList(CacheNames.GROUP_TASKS_BY_GROUP, cacheKey, GroupTaskResponseDTO.class);
            if (cachedList.isPresent()) {
                return cachedList.get();
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

        if (isActive && allTasksAreTrulyActive) {
            safeCacheService.put(CacheNames.GROUP_TASKS_BY_GROUP, cacheKey, response);
        }

        return response;
    }

    @Transactional(readOnly = true)
    public GroupTaskResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        GroupTask groupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        groupTaskSecurityService.validateCanSelect(groupTask, currentUser);

        boolean isActive = isActiveValidator.isGroupTaskActive(groupTask);
        if (!isActive) {
            groupTaskSecurityService.validateCanWorkGroupTask(currentUser, groupTask.getCreatorGroup());
        }

        var cachedDto = safeCacheService.get(CacheNames.GROUP_TASK, id, GroupTaskResponseDTO.class);
        if (cachedDto.isPresent()) {
            return cachedDto.get();
        }

        GroupTaskResponseDTO response = GroupTaskResponseDTO.from(groupTask);

        if (isActive) {
            safeCacheService.put(CacheNames.GROUP_TASK, id, response);
        }

        return response;
    }

    @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#dto.creatorGroupId() + ':' + #dto.intendedGroupId()")
    @Transactional
    public GroupTaskResponseDTO create(GroupTaskRequestDTO dto, AppUserDetails currentUser) {

        groupTaskSecurityService.validateGroupVisibility(dto.creatorGroupId(), dto.intendedGroupId(), currentUser);

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

        GroupTaskResponseDTO response = GroupTaskResponseDTO.from(saved);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.put(CacheNames.GROUP_TASK, saved.getId(), response);
                    var signal = new SseEventNames.GroupTaskSignalDTO(creatorGroup.getId(), intendedGroup.getId(),
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }

        return response;
    }

    @Transactional
    public GroupTaskBulkToAptekaResponseDTO createBulkToApteka(GroupTaskBulkToAptekaRequestDTO dto,
            AppUserDetails currentUser) {
        String cleanName = typeNameValidator.getCleanName(dto.name());
        List<UserGroup> intendedGroups = resolveAptekaIntendedGroups(dto.creatorGroupId(), currentUser);
        UserGroup creatorGroup = userGroupRepository.findById(dto.creatorGroupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.creatorGroupId()));
        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, creatorGroup);

        List<GroupTaskBulkItemResultDTO> items = new ArrayList<>();
        int created = 0;
        int skipped = 0;

        for (UserGroup intended : intendedGroups) {
            if (groupTaskRepository.existsByNameAndCreatorGroupIdAndIntendedGroupId(
                    cleanName, creatorGroup.getId(), intended.getId())) {
                GroupTask existing = groupTaskRepository
                        .findByGroupsAndIsActive(creatorGroup.getId(), intended.getId(), true)
                        .stream()
                        .filter(gt -> Objects.equals(gt.getName(), cleanName))
                        .findFirst()
                        .orElse(null);
                Integer existingId = existing != null ? existing.getId() : null;
                items.add(new GroupTaskBulkItemResultDTO(
                        intended.getId(), intended.getName(), existingId, "SKIPPED"));
                skipped++;
                continue;
            }

            GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                    .name(cleanName)
                    .creatorGroup(creatorGroup)
                    .intendedGroup(intended)
                    .isActive(true)
                    .build());

            if (dto.workTypes() != null) {
                for (var wtItem : dto.workTypes()) {
                    createWorkTypeOnGroupTask(saved, wtItem);
                }
            }

            items.add(new GroupTaskBulkItemResultDTO(
                    intended.getId(), intended.getName(), saved.getId(), "CREATED"));
            created++;

            final Integer gtId = saved.getId();
            final Integer creatorId = creatorGroup.getId();
            final Integer intendedId = intended.getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, creatorId + ":" + intendedId);
                        safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, gtId);
                    }
                });
            }
        }

        final Integer creatorId = creatorGroup.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.GroupTaskSignalDTO(creatorId, null, SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                    sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES,
                            new SseEventNames.WorkTypeSignalDTO(null, SseSignalTypes.CREATED));
                }
            });
        }

        return new GroupTaskBulkToAptekaResponseDTO(cleanName, creatorGroup.getId(), created, skipped, items);
    }

    @Transactional
    public GroupTaskResponseDTO update(Integer id, GroupTaskUpdateRequestDTO dto, AppUserDetails currentUser,
            Boolean confirm, Boolean syncToAllIntended) {
        GroupTask upGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        String oldName = upGroup.getName();
        Integer creatorId = upGroup.getCreatorGroup().getId();

        GroupTaskResponseDTO primary = updateOne(upGroup, dto, currentUser, confirm);

        if (Boolean.TRUE.equals(syncToAllIntended) && dto.name() != null) {
            String newCleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(oldName, newCleanName)) {
                List<GroupTask> siblings = groupTaskRepository.findSiblingAptekaGroupTasks(creatorId, oldName, true);
                for (GroupTask sibling : siblings) {
                    if (Objects.equals(sibling.getId(), id)) {
                        continue;
                    }
                    GroupTaskUpdateRequestDTO siblingDto = new GroupTaskUpdateRequestDTO(
                            newCleanName, null, null);
                    updateOne(sibling, siblingDto, currentUser, confirm);
                }
            }
        }

        return primary;
    }

    private GroupTaskResponseDTO updateOne(GroupTask upGroup, GroupTaskUpdateRequestDTO dto,
            AppUserDetails currentUser, Boolean confirm) {
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
                    safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, oldKey);
                    if (!oldKey.equals(newCacheKey)) {
                        safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, newCacheKey);
                    }
                    safeCacheService.put(CacheNames.GROUP_TASK, groupTaskId, finalResponse);

                    var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }

        return response;
    }

    @Transactional
    public void safeDelete(Integer id, AppUserDetails currentUser, Boolean syncToAllIntended) {
        GroupTask deletedGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        String name = deletedGroup.getName();
        Integer creatorId = deletedGroup.getCreatorGroup().getId();

        safeDeleteOne(deletedGroup, currentUser);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(creatorId, name, true)) {
                if (!Objects.equals(sibling.getId(), id)) {
                    safeDeleteOne(sibling, currentUser);
                }
            }
        }
    }

    private void safeDeleteOne(GroupTask deletedGroup, AppUserDetails currentUser) {
        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, deletedGroup.getCreatorGroup());
        if (isActiveValidator.isGroupTaskActive(deletedGroup)) {
            deletedGroup.setActive(false);
            Integer groupTaskId = deletedGroup.getId();
            String cacheKey = deletedGroup.getCreatorGroup().getId() + ":" + deletedGroup.getIntendedGroup().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeCacheService.evict(CacheNames.GROUP_TASK, groupTaskId);
                        safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, cacheKey);

                        var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void restore(Integer id, AppUserDetails currentUser, Boolean syncToAllIntended) {
        GroupTask restoredGroup = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        String name = restoredGroup.getName();
        Integer creatorId = restoredGroup.getCreatorGroup().getId();

        restoreOne(restoredGroup, currentUser);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(creatorId, name, false)) {
                if (!Objects.equals(sibling.getId(), id)) {
                    restoreOne(sibling, currentUser);
                }
            }
        }
    }

    private void restoreOne(GroupTask restoredGroup, AppUserDetails currentUser) {
        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, restoredGroup.getCreatorGroup());

        if (!isActiveValidator.isGroupTaskActive(restoredGroup)) {
            restoredGroup.setActive(true);
            Integer groupTaskId = restoredGroup.getId();
            String cacheKey = restoredGroup.getCreatorGroup().getId() + ":" + restoredGroup.getIntendedGroup().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, cacheKey);

                        var signal = new SseEventNames.EntityUpdateSignalDTO(groupTaskId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm, Boolean syncToAllIntended) {
        GroupTask deletedGroupTask = groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
        String name = deletedGroupTask.getName();
        Integer creatorId = deletedGroupTask.getCreatorGroup().getId();

        List<GroupTask> toDelete = new ArrayList<>();
        toDelete.add(deletedGroupTask);
        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(creatorId, name, null)) {
                if (!Objects.equals(sibling.getId(), id)) {
                    toDelete.add(sibling);
                }
            }
        }

        for (GroupTask gt : toDelete) {
            permanentDeleteOne(gt, confirm);
        }
    }

    private void permanentDeleteOne(GroupTask deletedGroupTask, Boolean confirm) {
        Integer id = deletedGroupTask.getId();
        groupTaskSecurityService.validateCanPermanentDelete(deletedGroupTask, confirm);
        groupTaskRepository.deleteById(id);

        String cachekey = deletedGroupTask.getCreatorGroup().getId().toString() + ':' +
                deletedGroupTask.getIntendedGroup().getId().toString();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.evict(CacheNames.GROUP_TASK, id);
                    safeCacheService.evict(CacheNames.GROUP_TASKS_BY_GROUP, cachekey);
                    safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, id);

                    var signal = new SseEventNames.GroupTaskSignalDTO(deletedGroupTask.getCreatorGroup().getId(),
                            deletedGroupTask.getIntendedGroup().getId(),
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_GROUP_TASKS, signal);
                }
            });
        }
    }

    List<UserGroup> resolveAptekaIntendedGroups(Integer creatorGroupId, AppUserDetails currentUser) {
        UserGroup creator = userGroupRepository.findByIdWithVisibleGroups(creatorGroupId)
                .orElseThrow(() -> new GroupUserNotFoundException(creatorGroupId));

        if (creator.getGroupType() != UserGroupType.EMPLOYEE_GROUP) {
            throw new InvalidGroupTaskException(
                    "Массовое создание на аптеки доступно только для группы типа EMPLOYEE_GROUP");
        }

        groupTaskSecurityService.validateCanWorkGroupTask(currentUser, creator);

        if (creator.getVisibleGroups() == null || creator.getVisibleGroups().isEmpty()) {
            throw new InvalidGroupTaskException("У отдела нет видимых групп аптек (visibleGroups)");
        }

        List<UserGroup> result = new ArrayList<>();
        for (UserGroup visible : creator.getVisibleGroups()) {
            if (!visible.isActive() || visible.getGroupType() != UserGroupType.APTEKA_GROUP) {
                continue;
            }
            groupTaskSecurityService.validateGroupVisibility(creator.getId(), visible.getId(), currentUser);
            result.add(visible);
        }

        if (result.isEmpty()) {
            throw new InvalidGroupTaskException("Нет видимых активных групп аптек (APTEKA_GROUP) для массового создания");
        }
        return result;
    }

    private void createWorkTypeOnGroupTask(GroupTask groupTask,
            GroupTaskBulkToAptekaRequestDTO.GroupTaskBulkWorkTypeItemDTO wtItem) {
        String cleanName = typeNameValidator.getCleanName(wtItem.name());
        if (workTypeRepository.existsByNameAndGroupTaskId(cleanName, groupTask.getId())) {
            return;
        }
        TaskPriority priority = StringUtils.hasText(wtItem.priorityCode())
                ? TaskPriority.fromCode(wtItem.priorityCode())
                : TaskPriority.LOW;
        WorkType.WorkTypeBuilder builder = WorkType.builder()
                .groupTask(groupTask)
                .name(cleanName)
                .priority(priority)
                .isActive(true);
        if (StringUtils.hasText(wtItem.wiki_link())) {
            builder.wikiLink(wtItem.wiki_link());
        }
        if (StringUtils.hasText(wtItem.commentForCreator())) {
            builder.commentForCreator(wtItem.commentForCreator());
        }
        workTypeRepository.save(builder.build());
    }

    private void validateGroupTaskName(String cleanName, Integer creatorGroupId, Integer intendedGroupId) {
        if (groupTaskRepository.existsByNameAndCreatorGroupIdAndIntendedGroupId(cleanName, creatorGroupId,
                intendedGroupId)) {
            throw new DuplicateGroupTaskException(cleanName);
        }
    }
}
