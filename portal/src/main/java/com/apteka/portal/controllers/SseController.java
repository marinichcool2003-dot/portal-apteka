package com.apteka.portal.controllers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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

@Tag(name = "SSE-уведомления", description = "Подписка на события портала в реальном времени (Server-Sent Events)")
@RestController
@RequestMapping("/api/v1/sse")
@Slf4j
public class SseController {
    private final Map<String, SseEmitter> emiters = new ConcurrentHashMap<>();

    @Operation(summary = "Подписка на SSE", description = "Открывает SSE-поток для текущего пользователя. Предыдущее подключение того же пользователя закрывается.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(Authentication authentication) {

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        UUID userId = userDetails.getInternalId();
        String userIdString = userId.toString();

        SseEmitter emitter = new SseEmitter(300_000L);

        SseEmitter previousEmitter = emiters.put(userIdString, emitter);
        if (previousEmitter != null) {
            previousEmitter.complete();
        }

        Runnable removeCallback = () -> {
            emiters.remove(userIdString, emitter);
        };

        emitter.onCompletion(removeCallback);
        emitter.onTimeout(removeCallback);
        emitter.onError((e) -> removeCallback.run());

        try {
            emitter.send(SseEmitter.event()
                    .name(SseEventNames.CONNECT)
                    .data("Успешное подключение к portal_apteka"));
        } catch (IOException e) {
            log.error("Ошибка при отправке стартового SSE события для {}", userIdString);
            emiters.remove(userIdString, emitter);
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
                emiters.remove(userIdString, emitter);
            }
        }
    }

    public void broadcastNotification(String eventName, Object data) {
        Iterator<Map.Entry<String, SseEmitter>> iterator = emiters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SseEmitter> entry = iterator.next();
            String userIdString = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.error("Не удалось отправить broadcast-уведомление для {}", userIdString);
                iterator.remove();
            }
        }
    }

    @Scheduled(fixedRate = 30_000)
    public void cleanDeadEmitters() {
        Iterator<Map.Entry<String, SseEmitter>> iterator = emiters.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SseEmitter> entry = iterator.next();
            try {
                entry.getValue().send(SseEmitter.event()
                        .name(SseEventNames.HEARTBEAT)
                        .data("ping"));
            } catch (IOException e) {
                log.debug("Удаление мёртвого SSE подключения: {}", entry.getKey());
                iterator.remove();
            }
        }
    }
}
