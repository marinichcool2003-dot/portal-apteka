package com.apteka.portal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    // 1. Настройки БД (подставляем те же локальные данные, что и в вашем launch.json)
    "DB_HOST=localhost",
    "DB_PORT=5432",
    "DB_NAME=portal",
    "DB_USERNAME=dir",
    "DB_PASSWORD=852456",
    
    // 2. Обязательный JWT секрет
    "JWT_SECRET=v7#Kq9@pjghjR$3xN!u8Y&j3LzWb$c4gld445!hhgfdssv",
    
    // 3. Данные администратора
    "ADMIN_LOGIN=admin",
    "ADMIN_PASSWORD=admin",
    
    // 4. Отключаем Redis на время тестов, чтобы они не зависели от кэша
    "spring.cache.type=none"
})
class PortalApplicationTests {
	@Test
	void contextLoads() {
	}

}
