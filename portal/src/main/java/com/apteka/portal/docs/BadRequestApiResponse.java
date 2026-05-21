package com.apteka.portal.docs;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.apteka.portal.models.ErrorResponse;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "400", description = "Ошибка валидации данных", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
public @interface BadRequestApiResponse {
    
}
