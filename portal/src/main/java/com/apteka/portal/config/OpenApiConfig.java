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
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Введите ваш JWT access токен в поле ниже")))
                .info(new Info()
                        .title("Портал Социальных аптек API")
                        .version("1.0.0")
                        .description("Документация REST API для Портала Социальных аптек")
                        .contact(new Contact()
                                .name("Birdux Dev Team")
                                .email("support@birdux.kz")
                                .url("https://rutube.ru/video/71a3f8b5315c645256c7fae5cbac3afe/")));
    }
}
