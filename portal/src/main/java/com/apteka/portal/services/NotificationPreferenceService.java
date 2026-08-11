package com.apteka.portal.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.notification.NotificationPreferenceItemDTO;
import com.apteka.portal.dtos.response.notification.NotificationPreferenceResponseDTO;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.NotificationChannel;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.models.NotificationPreference;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.NotificationPreferenceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public NotificationPreferenceResponseDTO getForCurrentUser(AppUserDetails currentUser) {
        UUID accountId = currentUser.getInternalId();
        List<NotificationPreference> stored = notificationPreferenceRepository.findByAccountId(accountId);

        Map<String, Boolean> storedMap = stored.stream()
                .collect(Collectors.toMap(
                        p -> key(p.getChannel(), p.getEventType()),
                        NotificationPreference::isEnabled,
                        (a, b) -> b));

        List<NotificationPreferenceItemDTO> items = new ArrayList<>();
        for (NotificationChannel channel : NotificationChannel.values()) {
            for (NotificationEventType eventType : NotificationEventType.values()) {
                boolean enabled = storedMap.getOrDefault(key(channel, eventType), false);
                items.add(new NotificationPreferenceItemDTO(channel, eventType, enabled));
            }
        }

        return new NotificationPreferenceResponseDTO(items);
    }

    @Transactional
    public NotificationPreferenceResponseDTO updateForCurrentUser(
            AppUserDetails currentUser,
            List<NotificationPreferenceItemDTO> preferences) {

        Account account = accountRepository.findById(currentUser.getInternalId())
                .orElseThrow();

        List<NotificationPreference> existing = notificationPreferenceRepository.findByAccountId(account.getId());
        Map<String, NotificationPreference> existingMap = existing.stream()
                .collect(Collectors.toMap(
                        p -> key(p.getChannel(), p.getEventType()),
                        p -> p,
                        (a, b) -> b));

        for (NotificationPreferenceItemDTO item : preferences) {
            String mapKey = key(item.channel(), item.eventType());
            NotificationPreference preference = existingMap.get(mapKey);
            if (preference == null) {
                preference = NotificationPreference.builder()
                        .account(account)
                        .channel(item.channel())
                        .eventType(item.eventType())
                        .enabled(item.enabled())
                        .build();
                notificationPreferenceRepository.save(preference);
            } else {
                preference.setEnabled(item.enabled());
            }
        }

        return getForCurrentUser(currentUser);
    }

    private static String key(NotificationChannel channel, NotificationEventType eventType) {
        return channel.name() + ":" + eventType.name();
    }
}
