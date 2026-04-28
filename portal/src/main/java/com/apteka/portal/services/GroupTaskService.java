package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.ClientRole;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UsersInApp;
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
    public GroupTask getOne(@PathVariable Integer id) {
        return groupTaskInterface.findById(id)
                .orElseThrow(() -> new GroupTaskNotFoundException(id));
    }

    @Transactional
    public GroupTask create(String name, UsersInApp currentUser) {
        validateGroupTaskName(name);
        validateCanCreateGroupTask(currentUser);

        return groupTaskInterface.save(GroupTask.builder()
                .name(name.strip())
                .userGroup(currentUser.getUserGroup())
                .build());
    }

    @Transactional
    public GroupTask update(Integer id, String name, UsersInApp currentUser) {
        GroupTask upGroup = getOne(id);

        validateCanUpdateGroupTask(upGroup, currentUser);
        validateGroupTaskName(name);

        upGroup.setName(name.strip());
        return groupTaskInterface.save(upGroup);
    }

    @Transactional
    public void delete(Integer id, UsersInApp currentUser) {
        GroupTask deletedTask = getOne(id);
        validateCanUpdateGroupTask(deletedTask, currentUser);
        groupTaskInterface.deleteById(id);
    }

    private void validateGroupTaskName(String name) {
        if (name == null || name.isBlank())
            throw new InvalidGroupTaskException(name);
        if (groupTaskInterface.findByName(name).isPresent())
            throw new DublicateGroupTaskException(name);
    }

    private void validateCanUpdateGroupTask(GroupTask groupTask, UsersInApp currentUser) {
        if(currentUser instanceof Client client) {
            if (client.getRole() == ClientRole.ADMIN) return;
            if (Objects.equals(client.getRole(), ClientRole.BOSS) && Objects.equals(client.getUserGroup().getId(), groupTask.getUserGroup().getId())) return;
            throw new AccessDeniedException("Изменить списи работ могут только начальники своего отдела");
        }
    }

    private void validateCanCreateGroupTask(UsersInApp currentUser) {
        if (currentUser instanceof Client client) {
            if (client.getRole() == ClientRole.ADMIN) return;
            if(Objects.equals(client.getRole(), ClientRole.BOSS)) return;
            throw new AccessDeniedException("Созда списи работ могут только начальники своего отдела");
        }
    }
}
