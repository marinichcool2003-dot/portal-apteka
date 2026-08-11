package com.apteka.portal.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.NotificationChannel;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.models.NotificationPreference;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.NotificationPreferenceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final AccountRepository accountRepository;
    private final MailService mailService;

    public void dispatchToUser(UUID accountId, NotificationEventType type, EmailContent content,
            Runnable ssePublish) {
        scheduleAfterCommit(ssePublish);
        notifyEmailIfEnabled(accountId, type, content);
    }

    public void notifyEmailIfEnabled(UUID accountId, NotificationEventType type, EmailContent content) {
        accountRepository.findById(accountId)
                .ifPresent(account -> notifyEmailIfEnabled(account, type, content));
    }

    public void notifyEmailIfEnabled(Account account, NotificationEventType type, EmailContent content) {
        if (account == null || !account.isActive() || !StringUtils.hasText(account.getEmail())) {
            return;
        }
        if (!isEmailEnabledForEvent(account.getId(), type)) {
            return;
        }
        mailService.send(account.getEmail(), content, type);
    }

    public void dispatchNewsToGroup(Integer userGroupId, EmailContent content, Runnable ssePublish) {
        scheduleAfterCommit(ssePublish);
        List<Account> members = accountRepository.findActiveByUserGroupId(userGroupId);
        for (Account member : members) {
            notifyEmailIfEnabled(member, NotificationEventType.NEWS, content);
        }
    }

    public boolean isEmailEnabledForEvent(UUID accountId, NotificationEventType eventType) {
        List<NotificationPreference> emailPrefs = notificationPreferenceRepository
                .findByAccountIdAndChannel(accountId, NotificationChannel.EMAIL);
        if (emailPrefs.isEmpty()) {
            return false;
        }
        boolean masterEnabled = emailPrefs.stream().anyMatch(NotificationPreference::isEnabled);
        if (!masterEnabled) {
            return false;
        }
        return notificationPreferenceRepository
                .findByAccountIdAndChannelAndEventType(accountId, NotificationChannel.EMAIL, eventType)
                .map(NotificationPreference::isEnabled)
                .orElse(false);
    }

    private void scheduleAfterCommit(Runnable action) {
        if (action == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }
}
