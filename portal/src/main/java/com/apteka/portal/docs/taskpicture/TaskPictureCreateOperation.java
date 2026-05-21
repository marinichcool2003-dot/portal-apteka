package com.apteka.portal.docs.taskpicture;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
    summary = "Загрузить изображение задачи",
    description = "Загружает изображение и прикрепляет его к задаче",
    responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Изображение успешно загружено"
        )
    }
)
public @interface TaskPictureCreateOperation {
}