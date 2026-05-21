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
    summary = "Получить изображение",
    description = "Возвращает изображение задачи по ID",
    responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Изображение успешно получено"
        )
    }
)
public @interface TaskPictureGetOperation {
}