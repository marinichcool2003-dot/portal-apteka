package com.apteka.portal.components;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.apteka.portal.controllers.SseController;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SseAfterCommitPublisher {
    private final SseController sseController;

    // AUDIT-FIX: S-14 — Defer observable notifications until the database transaction commits.
    public void publishAfterCommit(String eventName, Object payload) {
        Runnable publish = () -> sseController.broadcastNotification(eventName, payload);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish.run();
            }
        });
    }
}
