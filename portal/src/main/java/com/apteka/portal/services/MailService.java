package com.apteka.portal.services;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.models.MailOutbox;
import com.apteka.portal.models.MailOutboxStatus;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.repository.MailOutboxRepository;
import com.apteka.portal.util.HtmlUtils;

import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    /** Макс. попыток до REJECTED (даже для временных ошибок). */
    static final int MAX_ATTEMPTS = 10;

    /** Макс. задержка ретрая, сек (1 час). */
    static final long MAX_RETRY_DELAY_SECONDS = 3600L;

    private final MailOutboxRepository mailOutboxRepository;
    private final JavaMailSender mailSender;
    private final TransactionTemplate transactionTemplate;
    private final ResourceLoader resourceLoader;
    private final EmailTemplateService emailTemplateService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@farmp.ru}")
    private String mailFrom;

    @Value("${app.mail.brand.logo-path:classpath:logo.png}")
    private String logoPath;

    private Resource logoResource;

    @PostConstruct
    void initLogoResource() {
        Resource resource = resourceLoader.getResource(logoPath);
        logoResource = resource.exists() && resource.isReadable() ? resource : null;
    }

    /** @deprecated используйте {@link #send(String, EmailContent, NotificationEventType)} */
    public MailOutbox send(String toEmail, String subject, String bodyHtml, NotificationEventType eventType) {
        String plain = HtmlUtils.plainTextFromHtml(bodyHtml);
        return send(toEmail, new EmailContent(subject, bodyHtml, plain), eventType);
    }

    public MailOutbox send(String toEmail, EmailContent content, NotificationEventType eventType) {
        MailOutbox outbox = saveOutbox(toEmail, content, eventType);

        if (!mailEnabled) {
            log.info("Mail disabled; outbox record {} persisted as PENDING", outbox.getId());
            return outbox;
        }

        return attemptSend(outbox.getId());
    }

    /** @deprecated используйте {@link #sendAsync(String, EmailContent, NotificationEventType)} */
    @Async("mailExecutor")
    public void sendAsync(String toEmail, String subject, String bodyHtml, NotificationEventType eventType) {
        sendAsync(toEmail, new EmailContent(subject, bodyHtml, HtmlUtils.plainTextFromHtml(bodyHtml)), eventType);
    }

    @Async("mailExecutor")
    public void sendAsync(String toEmail, EmailContent content, NotificationEventType eventType) {
        MailOutbox outbox = saveOutbox(toEmail, content, eventType);

        if (!mailEnabled) {
            log.info("Mail disabled; outbox record {} persisted as PENDING", outbox.getId());
            return;
        }

        attemptSend(outbox.getId());
    }

    public MailOutbox attemptSend(MailOutbox outbox) {
        return attemptSend(outbox.getId());
    }

    public MailOutbox attemptSend(UUID outboxId) {
        MailOutbox outbox = mailOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new IllegalArgumentException("Mail outbox not found: " + outboxId));

        SendAttemptResult result = trySmtpSend(outbox);

        return transactionTemplate.execute(status -> {
            MailOutbox managed = mailOutboxRepository.findById(outboxId).orElseThrow();
            managed.setStatus(result.status());
            managed.setLastError(result.lastError());
            managed.setNextAttemptAt(result.nextAttemptAt());
            managed.setAttempts(managed.getAttempts() + 1);
            return mailOutboxRepository.save(managed);
        });
    }

    private MailOutbox saveOutbox(String toEmail, EmailContent content, NotificationEventType eventType) {
        return transactionTemplate.execute(status -> {
            MailOutbox outbox = MailOutbox.builder()
                    .toEmail(toEmail)
                    .subject(content.subject())
                    .bodyHtml(content.htmlBody())
                    .bodyPlain(content.plainBody())
                    .status(MailOutboxStatus.PENDING)
                    .attempts(0)
                    .eventType(eventType)
                    .build();
            return mailOutboxRepository.save(outbox);
        });
    }

    private SendAttemptResult trySmtpSend(MailOutbox outbox) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(outbox.getToEmail());
            helper.setSubject(outbox.getSubject());

            String plain = StringUtils.hasText(outbox.getBodyPlain())
                    ? outbox.getBodyPlain()
                    : HtmlUtils.plainTextFromHtml(outbox.getBodyHtml());
            helper.setText(plain, outbox.getBodyHtml());

            if (logoResource != null && emailTemplateService.isLogoAvailable()
                    && outbox.getBodyHtml().contains("cid:brandLogo")) {
                helper.addInline("brandLogo", logoResource);
            }

            mailSender.send(message);

            return SendAttemptResult.success();
        } catch (Exception e) {
            String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            log.warn("Failed to send mail to {}: {}", outbox.getToEmail(), error);

            int nextAttempts = outbox.getAttempts() + 1;
            if (isPermanentFailure(error) || nextAttempts >= MAX_ATTEMPTS) {
                log.warn("Mail to {} marked REJECTED (attempts={}, permanent={})",
                        outbox.getToEmail(), nextAttempts, isPermanentFailure(error));
                return SendAttemptResult.rejected(error);
            }
            return SendAttemptResult.failure(error, calculateNextAttempt(outbox.getAttempts()));
        }
    }

    public Instant calculateNextAttempt(int attempts) {
        int capped = Math.min(Math.max(0, attempts), 10);
        long delaySeconds = Math.min(MAX_RETRY_DELAY_SECONDS, (1L << capped) * 60L);
        return Instant.now().plusSeconds(delaySeconds);
    }

    static boolean isPermanentFailure(String error) {
        if (error == null || error.isBlank()) {
            return false;
        }
        String lower = error.toLowerCase(Locale.ROOT);
        return lower.contains("user unknown")
                || lower.contains("recipient address rejected")
                || lower.contains("invalid addresses")
                || lower.contains("mailbox unavailable")
                || lower.contains("550 5.1.1")
                || lower.contains("550 5.1.10")
                || lower.contains("address rejected");
    }

    private record SendAttemptResult(MailOutboxStatus status, String lastError, Instant nextAttemptAt) {
        static SendAttemptResult success() {
            return new SendAttemptResult(MailOutboxStatus.SENT, null, null);
        }

        static SendAttemptResult failure(String error, Instant nextAttemptAt) {
            return new SendAttemptResult(MailOutboxStatus.FAILED, error, nextAttemptAt);
        }

        static SendAttemptResult rejected(String error) {
            return new SendAttemptResult(MailOutboxStatus.REJECTED, error, null);
        }
    }
}
