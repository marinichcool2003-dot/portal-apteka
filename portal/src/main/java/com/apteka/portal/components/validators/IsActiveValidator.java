package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;

import com.apteka.portal.models.Account;
import com.apteka.portal.models.GroupMainPageLinks;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.MainPageLink;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;

@Component
public class IsActiveValidator {
    public boolean isUserGroupActive(UserGroup userGroup) {
        if (userGroup == null) {
            return false;
        }
        return userGroup.isActive();
    }
    public boolean isAccountActive(Account account) {
        if (account == null) {
            return false;
        }
        return account.isActive() && isUserGroupActive(account.getUserGroup());
    }
    public boolean isGroupTaskActive(GroupTask groupTask) {
        if (groupTask == null) {
            return false;
        }
        return groupTask.isActive() && isUserGroupActive(groupTask.getCreatorGroup()) && isUserGroupActive(groupTask.getIntendedGroup());
    }
    public boolean isWorkTypeActive(WorkType workType) {
        if (workType == null) {
            return false;
        }
        return workType.isActive() && isGroupTaskActive(workType.getGroupTask());
    }
    public boolean isGroupMainPageLinksActive(GroupMainPageLinks groupMainPageLinks) {
        if (groupMainPageLinks == null) {
            return false;
        }
        return groupMainPageLinks.isActive();
    }
    public boolean isMainPageLinkActive(MainPageLink mainPageLink) {
        if (mainPageLink == null) {
            return false;
        }
        return mainPageLink.isActive() && isGroupMainPageLinksActive(mainPageLink.getGroupMainPageLinks());
    }
}
