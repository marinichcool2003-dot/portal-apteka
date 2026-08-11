package com.apteka.portal.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.apteka.portal.exceptions.InvalidOtpException;
import com.apteka.portal.exceptions.OtpCooldownException;
import com.apteka.portal.exceptions.OtpMaxAttemptsExceededException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final String EMPLOYEE_RESET_PREFIX = "otp:employee-reset:";
    private static final String APTEKA_LOGIN_PREFIX = "otp:apteka-login:";
    private static final String ATTEMPTS_SUFFIX = ":attempts";
    private static final String COOLDOWN_SUFFIX = ":cooldown";

    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.ttl-seconds:600}")
    private long otpTtlSeconds;

    @Value("${app.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public enum OtpPurpose {
        EMPLOYEE_RESET,
        APTEKA_LOGIN
    }

    public String generateAndStore(OtpPurpose purpose, String login) {
        String cooldownKey = cooldownKey(purpose, login);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cooldownKey))) {
            throw new OtpCooldownException();
        }

        String code = generateCode();
        String hash = hashCode(code);
        String otpKey = otpKey(purpose, login);

        redisTemplate.opsForValue().set(otpKey, hash, otpTtlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(attemptsKey(purpose, login), "0", otpTtlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(cooldownKey, "1", resendCooldownSeconds, TimeUnit.SECONDS);

        return code;
    }

    public void verify(OtpPurpose purpose, String login, String code) {
        String otpKey = otpKey(purpose, login);
        String storedHash = redisTemplate.opsForValue().get(otpKey);

        if (storedHash == null) {
            throw new InvalidOtpException();
        }

        String attemptsKey = attemptsKey(purpose, login);
        String attemptsRaw = redisTemplate.opsForValue().get(attemptsKey);
        int attempts = attemptsRaw != null ? Integer.parseInt(attemptsRaw) : 0;

        if (attempts >= maxAttempts) {
            invalidate(purpose, login);
            throw new OtpMaxAttemptsExceededException();
        }

        if (!storedHash.equals(hashCode(code))) {
            redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), otpTtlSeconds, TimeUnit.SECONDS);
            throw new InvalidOtpException();
        }

        invalidate(purpose, login);
    }

    public void invalidate(OtpPurpose purpose, String login) {
        redisTemplate.delete(otpKey(purpose, login));
        redisTemplate.delete(attemptsKey(purpose, login));
    }

    private String generateCode() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }

    private String hashCode(String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(code.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private String otpKey(OtpPurpose purpose, String login) {
        return prefix(purpose) + login;
    }

    private String attemptsKey(OtpPurpose purpose, String login) {
        return prefix(purpose) + login + ATTEMPTS_SUFFIX;
    }

    private String cooldownKey(OtpPurpose purpose, String login) {
        return prefix(purpose) + login + COOLDOWN_SUFFIX;
    }

    private String prefix(OtpPurpose purpose) {
        return purpose == OtpPurpose.EMPLOYEE_RESET ? EMPLOYEE_RESET_PREFIX : APTEKA_LOGIN_PREFIX;
    }
}
