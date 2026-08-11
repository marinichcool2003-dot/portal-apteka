package com.apteka.portal.dtos.request.usergroup;

import java.util.Set;

import com.apteka.portal.models.UserGroupType;

public interface UserGroupRequestInterface {
    public String name();
    public String phoneNumber();
    public String internalNumber();
    public String extensionNumber();
    public Set<Integer> visibleGroups();
    public UserGroupType groupType();
}
