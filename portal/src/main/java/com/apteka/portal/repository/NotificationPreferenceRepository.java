package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.NotificationChannel;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.models.NotificationPreference;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {

    List<NotificationPreference> findByAccountId(UUID accountId);

    Optional<NotificationPreference> findByAccountIdAndChannelAndEventType(
            UUID accountId, NotificationChannel channel, NotificationEventType eventType);

    List<NotificationPreference> findByAccountIdAndChannel(UUID accountId, NotificationChannel channel);
}
