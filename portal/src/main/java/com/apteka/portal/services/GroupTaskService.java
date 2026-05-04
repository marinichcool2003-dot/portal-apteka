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
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.GroupTaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTaskService {

    private final GroupTaskRepository groupTaskRepository;
    private final UserGroupService userGroupService;
    private final GroupTaskSecurityService groupTaskSecurityService;
    private final CacheManager cacheManager;

    @Cacheable(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#userGroupId")
    @Transactional(readOnly = true)
    public List<GroupTask> getByUserGroup(Integer userGroupId) {
        userGroupService.getOne(userGroupId);
        return groupTaskRepository.findByUserGroupId(userGroupId);
    }

    @Cacheable(value = CacheNames.GROUP_TASK, key = "#id")
    @Transactional(readOnly = true)
    public GroupTask getOne(Integer id) {
        return groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
    }

    @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#dto.userGroupId()")
    @Transactional
    public GroupTask create(GroupTaskRequestDTO dto) {

        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        UserGroup userGroup = userGroupService.getOne(dto.userGroupId());
        String cleanName = dto.name().strip();

        validateGroupTaskName(cleanName, userGroup);
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, userGroup);

        GroupTask saved = groupTaskRepository.save(GroupTask.builder()
                .name(cleanName)
                .userGroup(userGroup)
                .build());

        return saved;
    }

    @Caching(evict = {
        @CacheEvict(value = CacheNames.GROUP_TASK, key = "#id"),
        @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#result.userGroup.id")
    })
    @Transactional
    public GroupTask update(Integer id, GroupTaskRequestDTO dto) {

        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        GroupTask upGroup = getOne(id);

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, upGroup.getUserGroup());

        String cleanName = dto.name().strip();

        if (Objects.equals(cleanName, upGroup.getName())) {
            return upGroup;
        }

        validateGroupTaskName(cleanName, upGroup.getUserGroup());
        upGroup.setName(cleanName);

        GroupTask saved = groupTaskRepository.save(upGroup);
  
        return saved;
    }

    @Transactional
    public void delete(Integer id) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        GroupTask deletedTask = getOne(id);
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, deletedTask.getUserGroup());
        groupTaskRepository.deleteById(id);

        cacheManager.getCache(CacheNames.GROUP_TASK).evict(id);
        cacheManager.getCache(CacheNames.GROUP_TASKS_BY_GROUP).evict(deletedTask.getUserGroup().getId());
        cacheManager.getCache(CacheNames.WORK_TYPES_BY_GROUP).evict(id);
    }

    private void validateGroupTaskName(String name, UserGroup userGroup) {
        if (name == null || name.isBlank()) {
            throw new InvalidGroupTaskException(name);
        }
        if (groupTaskRepository.findByNameAndUserGroupId(name, userGroup.getId()).isPresent())
            throw new DublicateGroupTaskException(name);
    }
}
