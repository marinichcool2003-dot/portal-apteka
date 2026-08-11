package com.apteka.portal.config;

import java.util.Locale;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// AUDIT-FIX: MAIL_ENCRYPTION=tls|ssl — настройка STARTTLS/SSL для JavaMailSender
@Component
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
@Slf4j
public class MailEncryptionConfig implements InitializingBean {

    private final JavaMailSender mailSender;

    @Value("${MAIL_ENCRYPTION:}")
    private String encryption;

    public MailEncryptionConfig(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void afterPropertiesSet() {
        if (mailSender instanceof JavaMailSenderImpl impl) {
            applyEncryption(impl, encryption);
            log.info("SMTP configured: host={}, port={}, encryption={}, from={}",
                    impl.getHost(), impl.getPort(),
                    encryption == null || encryption.isBlank() ? "none" : encryption.trim(),
                    impl.getUsername());
        }
    }

    static void applyEncryption(JavaMailSenderImpl sender, String encryption) {
        var props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        String mode = encryption == null ? "" : encryption.trim().toLowerCase(Locale.ROOT);
        boolean tls = "tls".equals(mode);
        boolean ssl = "ssl".equals(mode);
        props.put("mail.smtp.starttls.enable", Boolean.toString(tls));
        props.put("mail.smtp.starttls.required", Boolean.toString(tls));
        props.put("mail.smtp.ssl.enable", Boolean.toString(ssl));
        if (ssl) {
            // AUDIT-FIX: implicit SSL (порт 465) требует socketFactory
            props.put("mail.smtp.socketFactory.port", String.valueOf(sender.getPort()));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");
        }
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
    }
}
