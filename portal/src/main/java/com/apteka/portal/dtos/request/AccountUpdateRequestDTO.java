package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление аккаунта")
public record AccountUpdateRequestDTO(
		@Schema(description = "Логин пользователя")
		@Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен") String login,
		@Schema(description = "Пароль")
		@Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,
		@Schema(description = "Номер телефона")
		@Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789") String phoneNumber,
		@Schema(description = "Идентификатор группы пользователей")
		@Positive(message = "Группа аптеки должна быть больше нуля") Integer groupId) {
	public static AccountUpdateRequestDTO fromAptekaFullRequest(AptekaUpdateRequestDTO dto) {
		return new AccountUpdateRequestDTO(dto.login(), dto.password(), dto.phoneNumber(), dto.groupId());
	}
}
