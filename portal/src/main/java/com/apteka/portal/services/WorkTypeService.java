package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

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
    private final CacheManager cacheManager;
    private final GroupTaskRepository groupTaskRepository;
    private final TypeNameValidator typeNameValidator;
    private final SseController sseController;

    @Cacheable(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#groupTaskId", condition = "#isActive == true", sync = true)
    @Transactional(readOnly = true)
    public List<WorkTypeResponseDTO> getByGroupTask(Integer groupTaskId, AppUserDetails currentUser, Boolean isActive) {
        GroupTask groupTask = groupTaskRepository.findById(groupTaskId)
                .orElseThrow(() -> new GroupTaskNotFoundException(groupTaskId));
        if (Boolean.FALSE.equals(isActive)) {
            UserGroup userGroup = groupTask.getCreatorGroup();
            workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);
        }
        return workTypeRepository.findByGroupTaskIdAndIsActive(groupTaskId, isActive).stream()
                .map(WorkTypeResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public WorkTypeResponseDTO getOne(Integer id, AppUserDetails currentUser) {
        Cache cache = cacheManager.getCache(CacheNames.WORK_TYPE);
        if (cache != null) {
            WorkTypeResponseDTO cachedDto = cache.get(id, WorkTypeResponseDTO.class);
            if (cachedDto != null) {
                return cachedDto;
            }
        }

        WorkType workType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        boolean isActive = isActiveValidator.isWorkTypeActive(workType);

        if (!isActive) {
            UserGroup userGroup = workType.getGroupTask().getCreatorGroup();
            workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);
        }

        WorkTypeResponseDTO response = WorkTypeResponseDTO.from(workType);

        if (isActive && cache != null) {
            cache.put(id, response);
        }

        return response;
    }

    @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, key = "#result.taskGroup().id()")
    @Transactional
    public WorkTypeResponseDTO create(WorkTypeRequestDTO dto, AppUserDetails currentUser) {
        GroupTask groupTask = groupTaskRepository.findById(dto.groupTaskId())
                .orElseThrow(() -> new GroupTaskNotFoundException(dto.groupTaskId()));

        UserGroup userGroup = groupTask.getCreatorGroup();
        workTypeSecurityService.validateCanWorkWorkType(currentUser, userGroup);

        WorkType.WorkTypeBuilder workTypeBuilder = WorkType.builder();

        workTypeBuilder.groupTask(groupTask);

        if (!StringUtils.hasText(dto.name())) {
            throw new InvalidWorkTypeNameException();
        }

        String cleanWorkTypeName = typeNameValidator.getCleanName(dto.name());
        validateWorkTypeName(cleanWorkTypeName, dto.groupTaskId());
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
                    var cache = cacheManager.getCache(CacheNames.WORK_TYPE);
                    if (cache != null) {
                        cache.put(newWorkType.getId(), response);
                    }
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
            Boolean confirm) {

        WorkType upWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        boolean nameChanged = false;
        boolean groupsChanged = false;
        boolean anotherChanged = false;

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
                Integer oldGroupTaskId = upWorkType.getGroupTask().getId();
                var oldGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                if (oldGroupCache != null) {
                    oldGroupCache.evict(oldGroupTaskId);
                }
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

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var cache = cacheManager.getCache(CacheNames.WORK_TYPE);
                    if (cache != null && isActive) {
                        cache.put(id, finalResponse);
                    }

                    var workTypesByGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                    if (workTypesByGroupCache != null) {
                        workTypesByGroupCache.evict(response.taskGroup().id());
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
    public void safeDelete(Integer id, AppUserDetails currentUser) {
        WorkType deletedWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanWorkWorkType(currentUser, deletedWorkType.getGroupTask().getCreatorGroup());
        if (isActiveValidator.isWorkTypeActive(deletedWorkType)) {
            deletedWorkType.setActive(false);
            Integer deletedWorkTypeId = deletedWorkType.getId();
            Integer groupTaskId = deletedWorkType.getGroupTask().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var workTypeCache = cacheManager.getCache(CacheNames.WORK_TYPE);
                        if (workTypeCache != null) {
                            workTypeCache.evict(deletedWorkTypeId);
                        }

                        var workTypesByGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                        if (workTypesByGroupCache != null) {
                            workTypesByGroupCache.evict(groupTaskId);
                        }

                        var signal = new SseEventNames.EntityUpdateSignalDTO(deletedWorkTypeId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void restore(Integer id, AppUserDetails currentUser) {
        WorkType restoredWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanWorkWorkType(currentUser, restoredWorkType.getGroupTask().getCreatorGroup());
        if (!isActiveValidator.isWorkTypeActive(restoredWorkType)) {
            restoredWorkType.setActive(true);
            Integer restoredWorkTypeId = restoredWorkType.getId();
            Integer groupTaskId = restoredWorkType.getGroupTask().getId();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var workTypesByGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                        if (workTypesByGroupCache != null) {
                            workTypesByGroupCache.evict(groupTaskId);
                        }

                        var signal = new SseEventNames.EntityUpdateSignalDTO(restoredWorkTypeId,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_WORK_TYPES, signal);
                    }
                });
            }
        }
    }

    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm) {
        WorkType deletedWorkType = workTypeRepository.findByIdWithGroupTaskAndCreatorGroup(id)
                .orElseThrow(() -> new WorkTypeNotFoundException(id));

        workTypeSecurityService.validateCanPermanentDelete(deletedWorkType, confirm);
        workTypeRepository.delete(deletedWorkType);
        Integer groupTaskId = deletedWorkType.getGroupTask().getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var workTypeCache = cacheManager.getCache(CacheNames.WORK_TYPE);
                    if (workTypeCache != null) {
                        workTypeCache.evict(id);
                    }

                    var workTypesByGroupCache = cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP);
                    if (workTypesByGroupCache != null) {
                        workTypesByGroupCache.evict(groupTaskId);
                    }

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
