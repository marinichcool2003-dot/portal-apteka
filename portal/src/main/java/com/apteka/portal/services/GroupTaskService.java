package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;
import com.apteka.portal.repository.GroupTaskInterface;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupTaskService {

    private final GroupTaskInterface groupTaskInterface;
    private final UserGroupService userGroupService;

    @Transactional(readOnly = true)
    public List<GroupTask> getByUserGroup(Integer userGroupId) {
        userGroupService.getOne(userGroupId);
        return groupTaskInterface.findByUserGroupId(userGroupId);
    }

    @Transactional(readOnly = true)
    public GroupTask getOne(Integer id) {
        return groupTaskInterface.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
    }

    @Transactional
    public GroupTask create(String name, Integer userGroupId) {

        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        UserGroup userGroup = userGroupService.getOne(userGroupId);

        validateGroupTaskName(name, userGroup);
        validateBossOrAdmin(currentUser, userGroup);

        return groupTaskInterface.save(GroupTask.builder()
                .name(name.strip())
                .userGroup(Objects.requireNonNull(userGroup))
                .build());
    }

    @Transactional
    public GroupTask update(Integer id, String name) {

        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask upGroup = getOne(id);

        validateBossOrAdmin(currentUser, upGroup.getUserGroup());

        if (Objects.equals(name, upGroup.getName())) {
            return upGroup;
        }

        validateGroupTaskName(name, upGroup.getUserGroup());

        upGroup.setName(name.strip());
        return groupTaskInterface.save(upGroup);
    }

    @Transactional
    public void delete(Integer id) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        GroupTask deletedTask = getOne(id);
        validateBossOrAdmin(currentUser, deletedTask.getUserGroup());
        groupTaskInterface.deleteById(id);
    }

    private void validateGroupTaskName(String name, UserGroup userGroup) {
        if (name == null || name.isBlank())
            throw new InvalidGroupTaskException(name);
        if (groupTaskInterface.findByNameAndUserGroupId(name, userGroup.getId()).isPresent())
            throw new DublicateGroupTaskException(name);
    }

    private void validateBossOrAdmin(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.getType() != UserType.CLIENT) {
            throw new AccessDeniedException("Недопустимый тип пользователя");
        }

        boolean isAdmin = currentUser.getRoles().contains(UserRole.ADMIN);
        boolean isBossOfGroup = currentUser.getRoles().contains(UserRole.BOSS)
                && Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId());

        if (isAdmin || isBossOfGroup) {
            return;
        }
        throw new AccessDeniedException("Создать списки работ могут только начальники своего отдела");
    }
}
