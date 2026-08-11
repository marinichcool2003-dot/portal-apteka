package com.apteka.portal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class CookieSecureValidator {

    @Bean
    ApplicationRunner validateSecureCookies(@Value("${cookie.secure.flag:false}") boolean cookieSecureFlag) {
        return args -> {
            // AUDIT-FIX: понятное сообщение — часто ломается из-за COOKIE_SECURE_FLAG=false/пустого в .env
            if (!cookieSecureFlag) {
                throw new IllegalStateException(
                        "cookie.secure.flag must be true when the prod profile is active. "
                                + "Set COOKIE_SECURE_FLAG=true in the container environment (docker-compose already does). "
                                + "For HTTP-only dev use SPRING_PROFILES_ACTIVE without prod.");
            }
        };
    }
}
