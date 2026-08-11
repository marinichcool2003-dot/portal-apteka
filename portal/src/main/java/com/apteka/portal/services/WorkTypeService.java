package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.cache.SafeCacheService;
import com.apteka.portal.components.servicesecurity.WorkTypeSecurityService;
import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.WorkTypeRequestDTO;
import com.apteka.portal.dtos.request.WorkTypeUpdateRequestDTO;
import com.apteka.portal.dtos.response.WorkTypeResponseDTO;
import com.apteka.portal.exceptions.DuplicateWorkTypeNameException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.GroupTaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkTypeService {
    private final WorkTypeRepository workTypeRepository;
    private final WorkTypeSecurityService workTypeSecurityService;
    private final IsActiveValidator isActiveValidator;
    private final SafeCacheService safeCacheService;
    private final GroupTaskRepository groupTaskRepository;
    private final TypeNameValidator typeNameValidator;
    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<WorkTypeResponseDTO> getByGroupTask(Integer groupTaskId, AppUserDetails currentUser, Boolean isActive) {
        GroupTask groupTask = groupTaskRepository.findById(groupTaskId)
                .orElseThrow(() -> new GroupTaskNotFoundException(groupTaskId));
        workTypeSecurityService.validateCanSelectGroupTask(groupTask, currentUser);
        if (Boolean.FALSE.equals(isActive)) {
            UserGroup userGroup = groupTask.getCreatorGroup();
            workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);
        }

        if (Boolean.TRUE.equals(isActive)) {
            var cachedList = safeCacheService.getList(CacheNames.WORK_TYPES_BY_GROUP, groupTaskId,
                    WorkTypeResponseDTO.class);
            if (cachedList.isPresent()) {
                return cachedList.get();
            }
        }

        List<WorkTypeResponseDTO> response = workTypeRepository.findByGroupTaskIdAndIsActive(groupTaskId, isActive).stream()
                .map(WorkTypeResponseDTO::from).toList();
        if (Boolean.TRUE.equals(isActive)) {
            safeCacheService.put(CacheNames.WORK_TYPES_BY_GROUP, groupTaskId, response);
        }
        return response;
    }

    @Transactional(readOnly = true)
    public WorkTypeResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        WorkType workType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanSelect(workType, currentUser);
        boolean isActive = isActiveValidator.isWorkTypeActive(workType);
        if (!isActive) {
            UserGroup userGroup = workType.getGroupTask().getCreatorGroup();
            workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);
        }

        var cachedDto = safeCacheService.get(CacheNames.WORK_TYPE, id, WorkTypeResponseDTO.class);
        if (cachedDto.isPresent()) {
            return cachedDto.get();
        }

        WorkTypeResponseDTO response = WorkTypeResponseDTO.from(workType);
        if (isActive) {
            safeCacheService.put(CacheNames.WORK_TYPE, id, response);
        }

        return response;
    }

    @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#result.taskGroup().id()")
    @Transactional
    public WorkTypeResponseDTO create(WorkTypeRequestDTO dto, AppUserDetails currentUser, Boolean syncToAllIntended) {
        GroupTask groupTask = groupTaskRepository.findById(dto.groupTaskId())
                .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));

        WorkTypeResponseDTO primary = createOnGroupTask(groupTask, dto, currentUser, false);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            List<GroupTask> siblings = groupTaskRepository.findSiblingAptekaGroupTasks(
                    groupTask.getCreatorGroup().getId(), groupTask.getName(), true);
            for (GroupTask sibling : siblings) {
                if (Objects.equals(sibling.getId(), groupTask.getId())) {
                    continue;
                }
                if (workTypeRepository.existsByNameAndGroupTaskId(cleanName, sibling.getId())) {
                    continue;
                }
                WorkTypeRequestDTO siblingDto = new WorkTypeRequestDTO(
                        dto.name(), sibling.getId(), dto.priorityCode(), dto.commentForCreator(), dto.wiki_link());
                createOnGroupTask(sibling, siblingDto, currentUser, true);
            }
        }

        return primary;
    }

    @Transactional
    public WorkTypeResponseDTO create(WorkTypeRequestDTO dto, AppUserDetails currentUser) {
        return create(dto, currentUser, false);
    }

    private WorkTypeResponseDTO createOnGroupTask(GroupTask groupTask, WorkTypeRequestDTO dto,
            AppUserDetails currentUser, boolean skipIfDuplicate) {
        UserGroup userGroup = groupTask.getCreatorGroup();
        workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);

        if (!StringUtils.hasText(dto.name())) {
            throw new InvalidWorkTypeNameException();
        }

        String cleanWorkTypeName = typeNameValidator.getCleanName(dto.name());
        if (skipIfDuplicate && workTypeRepository.existsByNameAndGroupTaskId(cleanWorkTypeName, groupTask.getId())) {
            return workTypeRepository.findByNameAndGroupTaskId(cleanWorkTypeName, groupTask.getId())
                    .map(WorkTypeResponseDTO::from)
                    .orElse(null);
        }
        validateWorkTypeName(cleanWorkTypeName, groupTask.getId());

        WorkType.WorkTypeBuilder workTypeBuilder = WorkType.builder();
        workTypeBuilder.groupTask(groupTask);
        workTypeBuilder.name(cleanWorkTypeName);

        if (StringUtils.hasText(dto.priorityCode())) {
            workTypeBuilder.priority(TaskPriority.fromCode(dto.priorityCode()));
        } else {
            workTypeBuilder.priority(TaskPriority.LOW);
        }

        if (StringUtils.hasText(dto.wiki_link())) {
            workTypeBuilder.wikiLink(dto.wiki_link());
        }

        if (StringUtils.hasText(dto.commentForCreator())) {
            workTypeBuilder.commentForCreator(dto.commentForCreator());
        }

        WorkType newWorkType = workTypeRepository.save(workTypeBuilder.isActive(true).build());
        WorkTypeResponseDTO response = WorkTypeResponseDTO.from(newWorkType);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.put(CacheNames.WORK_TYPE, newWorkType.getId(), response);
                    safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, groupTask.getId());
                    var signal = new SseEventNames.WorkTypeSignalDTO(newWorkType.getGroupTask().getId(),
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                }
            });
        }
        return response;
    }

    @Transactional
    public WorkTypeResponseDTO update(Integer id, WorkTypeUpdateRequestDTO dto, AppUserDetails currentUser,
            Boolean confirm, Boolean syncToAllIntended) {

        WorkType upWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));
        String oldName = upWorkType.getName();
        GroupTask sourceGroupTask = upWorkType.getGroupTask();
        Integer creatorId = sourceGroupTask.getCreatorGroup().getId();
        String groupTaskName = sourceGroupTask.getName();

        WorkTypeResponseDTO primary = updateOne(upWorkType, dto, currentUser, confirm);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            List<GroupTask> siblings = groupTaskRepository.findSiblingAptekaGroupTasks(creatorId, groupTaskName, true);
            for (GroupTask sibling : siblings) {
                if (Objects.equals(sibling.getId(), sourceGroupTask.getId())) {
                    continue;
                }
                workTypeRepository.findByNameAndGroupTaskId(oldName, sibling.getId()).ifPresent(siblingWt -> {
                    // groupTaskId в DTO не переносим на siblings
                    WorkTypeUpdateRequestDTO siblingDto = new WorkTypeUpdateRequestDTO(
                            dto.name(), null, dto.priorityCode(), dto.commentForCreator(), dto.wiki_link());
                    updateOne(siblingWt, siblingDto, currentUser, confirm);
                });
            }
        }

        return primary;
    }

    @Transactional
    public WorkTypeResponseDTO update(Integer id, WorkTypeUpdateRequestDTO dto, AppUserDetails currentUser,
            Boolean confirm) {
        return update(id, dto, currentUser, confirm, false);
    }

    private WorkTypeResponseDTO updateOne(WorkType upWorkType, WorkTypeUpdateRequestDTO dto,
            AppUserDetails currentUser, Boolean confirm) {
        boolean nameChanged = false;
        boolean groupsChanged = false;
        boolean anotherChanged = false;
        Integer oldGroupTaskId = null;

        if (dto.name() != null) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upWorkType.getName())) {
                validateWorkTypeName(cleanName, upWorkType.getGroupTask().getId());
                upWorkType.setName(cleanName);
                nameChanged = true;
            }
        }

        if (StringUtils.hasText(dto.priorityCode())) {
            TaskPriority newPriority = TaskPriority.fromCode(dto.priorityCode());
            if (!Objects.equals(newPriority, upWorkType.getPriority())) {
                upWorkType.setPriority(TaskPriority.fromCode(dto.priorityCode()));
                anotherChanged = true;
            }
        }

        if (StringUtils.hasText(dto.wiki_link()) && !Objects.equals(dto.wiki_link(), upWorkType.getWikiLink())) {
            upWorkType.setWikiLink(dto.wiki_link());
            anotherChanged = true;
        }

        if (StringUtils.hasText(dto.commentForCreator())
                && !Objects.equals(dto.commentForCreator(), upWorkType.getCommentForCreator())) {
            upWorkType.setCommentForCreator(dto.commentForCreator());
            anotherChanged = true;
        }

        if (dto.groupTaskId() != null && dto.groupTaskId() > 0) {
            GroupTask newGroupTask = groupTaskRepository.findById(dto.groupTaskId())
                    .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));
            if (!Objects.equals(newGroupTask.getId(), upWorkType.getGroupTask().getId())) {
                validateWorkTypeName(upWorkType.getName(), newGroupTask.getId());
                oldGroupTaskId = upWorkType.getGroupTask().getId();
                upWorkType.setGroupTask(newGroupTask);
                groupsChanged = true;
            }
        }

        boolean hasChanged = nameChanged || groupsChanged || anotherChanged;

        if (hasChanged) {
            boolean isConfirm = Boolean.TRUE.equals(confirm);
            workTypeSecurityService.validateWorkTypeUpdate(currentUser, upWorkType, nameChanged, groupsChanged,
                    isConfirm);
            upWorkType.setUpdatedBy(currentUser.getDisplayName());

            upWorkType = workTypeRepository.save(upWorkType);
        }
        WorkTypeResponseDTO response = WorkTypeResponseDTO.from(upWorkType);

        if (hasChanged && TransactionSynchronizationManager.isSynchronizationActive()) {

            final WorkTypeResponseDTO finalResponse = response;
            final Integer workTypeId = upWorkType.getId();
            final boolean isActive = isActiveValidator.isWorkTypeActive(upWorkType);
            final Integer previousGroupTaskId = oldGroupTaskId;
            final Integer id = upWorkType.getId();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    if (isActive) {
                        safeCacheService.put(CacheNames.WORK_TYPE, id, finalResponse);
                    }
                    safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, response.taskGroup().id());
                    if (previousGroupTaskId != null) {
                        safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, previousGroupTaskId);
                    }

                    var signal = new SseEventNames.EntityUpdateSignalDTO(workTypeId,
                            SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                }
            });
        }

        return response;
    }

    @Transactional
    public void safeDelete(Integer id, AppUserDetails currentUser, Boolean syncToAllIntended) {
        WorkType deletedWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));
        String name = deletedWorkType.getName();
        GroupTask sourceGt = deletedWorkType.getGroupTask();

        safeDeleteOne(deletedWorkType, currentUser);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(
                    sourceGt.getCreatorGroup().getId(), sourceGt.getName(), true)) {
                if (Objects.equals(sibling.getId(), sourceGt.getId())) {
                    continue;
                }
                workTypeRepository.findByNameAndGroupTaskId(name, sibling.getId())
                        .ifPresent(wt -> safeDeleteOne(wt, currentUser));
            }
        }
    }

    @Transactional
    public void safeDelete(Integer id, AppUserDetails currentUser) {
        safeDelete(id, currentUser, false);
    }

    private void safeDeleteOne(WorkType deletedWorkType, AppUserDetails currentUser) {
        workTypeSecurityService.validateCanWorkWorkType(currentUser, deletedWorkType.getGroupTask().getCreatorGroup());
        if (isActiveValidator.isWorkTypeActive(deletedWorkType)) {
            deletedWorkType.setActive(false);
            Integer deletedWorkTypeId = deletedWorkType.getId();
            Integer groupTaskId = deletedWorkType.getGroupTask().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeCacheService.evict(CacheNames.WORK_TYPE, deletedWorkTypeId);
                        safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, groupTaskId);

                        var signal = new SseEventNames.EntityUpdateSignalDTO(deletedWorkTypeId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void restore(Integer id, AppUserDetails currentUser, Boolean syncToAllIntended) {
        WorkType restoredWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));
        String name = restoredWorkType.getName();
        GroupTask sourceGt = restoredWorkType.getGroupTask();

        restoreOne(restoredWorkType, currentUser);

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(
                    sourceGt.getCreatorGroup().getId(), sourceGt.getName(), true)) {
                if (Objects.equals(sibling.getId(), sourceGt.getId())) {
                    continue;
                }
                workTypeRepository.findByNameAndGroupTaskId(name, sibling.getId())
                        .ifPresent(wt -> restoreOne(wt, currentUser));
            }
        }
    }

    @Transactional
    public void restore(Integer id, AppUserDetails currentUser) {
        restore(id, currentUser, false);
    }

    private void restoreOne(WorkType restoredWorkType, AppUserDetails currentUser) {
        workTypeSecurityService.validateCanWorkWorkType(currentUser, restoredWorkType.getGroupTask().getCreatorGroup());
        if (!isActiveValidator.isWorkTypeActive(restoredWorkType)) {
            restoredWorkType.setActive(true);
            Integer restoredWorkTypeId = restoredWorkType.getId();
            Integer groupTaskId = restoredWorkType.getGroupTask().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, groupTaskId);

                        var signal = new SseEventNames.EntityUpdateSignalDTO(restoredWorkTypeId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm, Boolean syncToAllIntended) {
        WorkType deletedWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));
        String name = deletedWorkType.getName();
        GroupTask sourceGt = deletedWorkType.getGroupTask();

        if (Boolean.TRUE.equals(syncToAllIntended)) {
            for (GroupTask sibling : groupTaskRepository.findSiblingAptekaGroupTasks(
                    sourceGt.getCreatorGroup().getId(), sourceGt.getName(), null)) {
                if (Objects.equals(sibling.getId(), sourceGt.getId())) {
                    continue;
                }
                workTypeRepository.findByNameAndGroupTaskId(name, sibling.getId())
                        .ifPresent(wt -> permanentDeleteOne(wt, confirm));
            }
        }

        permanentDeleteOne(deletedWorkType, confirm);
    }

    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm) {
        permanentDelete(id, currentUser, confirm, false);
    }

    private void permanentDeleteOne(WorkType deletedWorkType, Boolean confirm) {
        Integer id = deletedWorkType.getId();
        workTypeSecurityService.validateCanPermanentDelete(deletedWorkType, confirm);
        Integer groupTaskId = deletedWorkType.getGroupTask().getId();
        workTypeRepository.delete(deletedWorkType);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.evict(CacheNames.WORK_TYPE, id);
                    safeCacheService.evict(CacheNames.WORK_TYPES_BY_GROUP, groupTaskId);

                    var signal = new SseEventNames.WorkTypeSignalDTO(id, SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                }
            });
        }
    }

    private void validateWorkTypeName(String name, Integer groupTaskId) {
        boolean exists = workTypeRepository.existsByNameAndGroupTaskId(name, groupTaskId);
        if (exists) {
            throw new DuplicateWorkTypeNameException(name);
        }
    }
}
