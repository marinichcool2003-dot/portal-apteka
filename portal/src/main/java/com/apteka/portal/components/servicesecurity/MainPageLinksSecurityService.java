package com.apteka.portal.components.servicesecurity;

import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;

@Component
public class MainPageLinksSecurityService {

    public void validateCanSelectNonActive(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_NON_ACTIVE_MAIN_PAGE_LINK)) {
            return;
        }
    }
}
