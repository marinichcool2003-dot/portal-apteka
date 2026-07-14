package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;

import com.apteka.portal.models.Account;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;

@Component
public class IsActiveValidator {
    public boolean isUserGroupActive(UserGroup userGroup) {
        return userGroup.isActive();
    }
    public boolean isAccountActive(Account account) {
        return account.isActive() && isUserGroupActive(account.getUserGroup());
    }
    public boolean isGroupTaskActive(GroupTask groupTask) {
        return groupTask.isActive() && isUserGroupActive(groupTask.getCreatorGroup()) && isUserGroupActive(groupTask.getExecutorGroup());
    }
    public boolean isWorkTypeActive(WorkType workType) {
        return workType.isActive() && isGroupTaskActive(workType.getGroupTask());
    }
}
