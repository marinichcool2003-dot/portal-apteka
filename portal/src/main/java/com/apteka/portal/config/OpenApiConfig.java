package com.apteka.portal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenApi() {
        final String securitySchemeName = "cookieAuth";

        OpenAPI openApi = new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name("X-Access-Token") // Имя куки, которую ищет бэкенд
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .description("Авторизация на основе Cookies. Выполните эндпоинт /login. " +
                                             "Браузер автоматически сохранит куку X-Access-Token, " +
                                             "и Swagger будет прикреплять её ко всем последующим запросам.")))
                .info(new Info()
                        .title("Портал Социальных аптек API")
                        .version("1.0.0")
                        .description("Документация REST API для Портала Социальных аптек")
                        .contact(new Contact()
                                .name("Birdux Dev Team")
                                .email("support@birdux.kz")
                                .url("https://rutube.ru/video/71a3f8b5315c645256c7fae5cbac3afe/")));

        // КРИТИЧЕСКИЙ ШАГ: заставляем Swagger-UI автоматически отправлять куки в Docker-окружении
        openApi.addExtension("x-requestInterceptor",
                "req => { req.credentials = 'include'; return req; }");

        return openApi;
    }
}

