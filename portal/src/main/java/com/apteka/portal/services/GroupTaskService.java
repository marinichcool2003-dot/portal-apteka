package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.GroupTaskSecurityService;
import com.apteka.portal.dtos.request.GroupTaskRequestDTO;
import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.AppUserDetails;
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

    @Transactional(readOnly = true)
    public List<GroupTask> getByUserGroup(Integer userGroupId) {
        userGroupService.getOne(userGroupId);
        return groupTaskRepository.findByUserGroupId(userGroupId);
    }

    @Transactional(readOnly = true)
    public GroupTask getOne(Integer id) {
        return groupTaskRepository.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
    }

    @Transactional
    public GroupTask create(GroupTaskRequestDTO dto) {

        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        UserGroup userGroup = userGroupService.getOne(dto.userGroupId());

        validateGroupTaskName(dto.name(), userGroup);
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, userGroup);

        return groupTaskRepository.save(GroupTask.builder()
                .name(dto.name().strip())
                .userGroup(Objects.requireNonNull(userGroup))
                .build());
    }

    @Transactional
    public GroupTask update(Integer id, GroupTaskRequestDTO dto) {

        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask upGroup = getOne(id);

        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, upGroup.getUserGroup());

        if (Objects.equals(dto.name(), upGroup.getName())) {
            return upGroup;
        }

        validateGroupTaskName(dto.name(), upGroup.getUserGroup());

        upGroup.setName(dto.name().strip());
        return groupTaskRepository.save(upGroup);
    }

    @Transactional
    public void delete(Integer id) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask deletedTask = getOne(id);
        groupTaskSecurityService.validateBossOrAdminInGroup(currentUser, deletedTask.getUserGroup());
        groupTaskRepository.deleteById(id);
    }

    private void validateGroupTaskName(String name, UserGroup userGroup) {
        if (name == null || name.isBlank())
            throw new InvalidGroupTaskException(name);
        if (groupTaskRepository.findByNameAndUserGroupId(name, userGroup.getId()).isPresent())
            throw new DublicateGroupTaskException(name);
    }  
}
