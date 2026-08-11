package com.apteka.portal.services;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.MailOutbox;
import com.apteka.portal.models.MailOutboxStatus;
import com.apteka.portal.repository.MailOutboxRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailOutboxRetryScheduler {

    private final MailOutboxRepository mailOutboxRepository;
    private final MailService mailService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Scheduled(cron = "${app.mail.retry.cron:0 */5 * * * *}")
    public void retryFailedMails() {
        if (!mailEnabled) {
            return;
        }

        List<MailOutbox> pending = mailOutboxRepository.findReadyForRetry(
                List.of(MailOutboxStatus.PENDING, MailOutboxStatus.FAILED),
                Instant.now());

        int ok = 0;
        int errors = 0;
        for (MailOutbox outbox : pending) {
            try {
                mailService.attemptSend(outbox);
                ok++;
            } catch (Exception e) {
                errors++;
                log.error("Mail outbox retry failed for id={}, to={}: {}",
                        outbox.getId(), outbox.getToEmail(), e.getMessage(), e);
            }
        }

        if (!pending.isEmpty()) {
            log.debug("Processed {} mail outbox retries (ok={}, errors={})", pending.size(), ok, errors);
        }
    }
}
