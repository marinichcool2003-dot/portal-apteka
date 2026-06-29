package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.SseEventNames;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/sse")
@Tag(name = "Уведомления (SSE)")
@Slf4j
public class SseController {
    private final Map<String, SseEmitter> emiters = new ConcurrentHashMap<>();

    @Operation(summary = "Подключение к потоку уведомлений")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getInternalId();
        String userIdString = userId.toString();

        SseEmitter emitter = new SseEmitter(300_000L);

        emiters.put(userIdString, emitter);

        emitter.onCompletion(() -> emiters.remove(userIdString));
        emitter.onTimeout(() -> emiters.remove(userIdString));
        emitter.onError((e) -> emiters.remove(userIdString));

        try {
            emitter.send(SseEmitter.event()
                    .name(SseEventNames.CONNECT)
                    .data("Успешное подключение к portal_apteka"));
        } catch (IOException e) {
            log.error("Ошибка при отправке стартового SSE события для {}", userIdString);
            emiters.remove(userIdString);
        }
        return emitter;
    }

    public void sendNotification(String userIdString, String eventName, Object data) {
        SseEmitter emitter = emiters.get(userIdString);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("Не удалось отправить SSE уведомление для {}", userIdString, e);
                emiters.remove(userIdString);
            }
        }
    }

    public void broadcastNotification(String eventName, Object data) {
        emiters.forEach((userIdString, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("Не удалось отправить broadcast-уведомление для {}", userIdString);
                emiters.remove(userIdString);
            }
        });
    }
}
