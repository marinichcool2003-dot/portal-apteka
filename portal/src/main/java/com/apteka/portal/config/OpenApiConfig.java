package com.apteka.portal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI(){
        final String accessCookieName = "Access";
        final String refreshCookieName = "Refresh";

        return new OpenAPI()
                .info(new Info()
                        .title("API портал Социальных аптек")
                        .version("1.0.0")
                        .description("Документация тестового проекта"))
                .components(new Components()
                        // 1. Схема для Access токена в куках
                        .addSecuritySchemes("AccessCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(accessCookieName)
                                .description("Введите значение JWT Access токена"))
                        // 2. Схема для Refresh токена в куках
                        .addSecuritySchemes("RefreshCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name(refreshCookieName)
                                .description("Введите значение JWT Refresh токена (для эндпоинтов обновления сессии)"))
                )
                // 3. Глобально связываем схемы с запросами
                .addSecurityItem(new SecurityRequirement()
                        .addList("AccessCookie")
                        .addList("RefreshCookie"));
    }
}
