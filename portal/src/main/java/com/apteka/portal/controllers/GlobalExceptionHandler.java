package com.apteka.portal.controllers;

import java.util.stream.Collectors;

import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.AptekaCreateException;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.AsyncCommentException;
import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.exceptions.ClientBelongsToAnotherGroupException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateAptekaFullNameException;
import com.apteka.portal.exceptions.DublicateAptekaLoginException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.DublicateGroupUserException;
import com.apteka.portal.exceptions.DublicateWorkTypeNameException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidAptekaAdressException;
import com.apteka.portal.exceptions.InvalidAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaNumberException;
import com.apteka.portal.exceptions.InvalidAptekaPasswordException;
import com.apteka.portal.exceptions.InvalidAptekaPhoneNumberException;
import com.apteka.portal.exceptions.InvalidClientFullNameException;
import com.apteka.portal.exceptions.InvalidClientLoginException;
import com.apteka.portal.exceptions.InvalidClientPasswordException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.exceptions.InvalidRefreshTokenException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.InvalidWorkTypeNameException;
import com.apteka.portal.exceptions.NewsNotFoundException;
import com.apteka.portal.exceptions.SelfDeleteException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.TaskPictureNotFoundException;
import com.apteka.portal.exceptions.UnknowRoleException;
import com.apteka.portal.exceptions.UnknowTaskPriorityException;
import com.apteka.portal.exceptions.UnknowTaskStatusException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.ErrorResponse;

import io.jsonwebtoken.io.IOException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AlreadyHaveThisPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIOException(AlreadyHaveThisPasswordException e) {
        log.error("Ошибка сохранения пароля: {}", e.getMessage());
        String errorMessage = "Ошибка! Пароль не сохранен: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(AptekaCreateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAptekaCreateException(AptekaCreateException e) {
        log.error("Аптека не создана: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Аптека не создана: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(AptekaNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleAptekaNotFoundException(AptekaNotFoundException e) {
        log.error("Аптека не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Аптека не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleBadCredentialsException(BadCredentialsException e) {
        log.error("Ошибка! Неверный логин или пароль: {}", e.getMessage());
        String errorMessage = "Неверный логин или пароль: " + e.getMessage();
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(AsyncCommentException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAsyncCommentException(AsyncCommentException e) {
        log.error("Ошибка! Не удалось сохранить комментарий асинхронно: {}", e.getMessage());
        String errorMessage = "Не удалось сохранить комментарий асинхронно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(NewsNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNewsNotFoundException(NewsNotFoundException e) {
        log.error("Новость не найдена: {}", e.getMessage(), e);
        String errorMessage = "Новость не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(UnknowRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnknowRoleException(UnknowRoleException e) {
        log.warn("Ошибка присвоения роли: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка присвоения роли: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(AvtorCommentNotInputException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAvtorCommentNotInputException(AvtorCommentNotInputException e) {
        log.error("Доступ запрещен: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Доступ запрещен: " + e.getMessage();
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(BlockChangeIfNotActuallyTaskException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBlockChangeIfNotActuallyTaskException(BlockChangeIfNotActuallyTaskException e) {
        log.error("Ошибка изменения задачи: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка изменения задачи: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(UnknowTaskStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnknowTaskStatusException(UnknowTaskStatusException e) {
        log.warn("Ошибка изменения задач: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка при изменении задачи: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(UnknowTaskPriorityException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnknowTaskPriorityException(UnknowTaskPriorityException e) {
        log.warn("Ошибка изменения задач: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка при изменении задачи: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(ClientBelongsToAnotherGroupException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleClientBelongsToAnotherGroupException(ClientBelongsToAnotherGroupException e) {
        log.error("Доступ запрещен: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Доступ запрещен: " + e.getMessage();
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateAptekaFullNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateAptekaFullNameException(DublicateAptekaFullNameException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateClientLoginException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateClientLoginException(DublicateClientLoginException e) {
        log.warn("Ошибка изменения сотрудников: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка изменения сотрудников: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateWorkTypeNameException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateWorkTypeNameException(DublicateWorkTypeNameException e) {
        log.warn("Ошибка изменения вида работ: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка изменения вида работ: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIOException(IOException e) {
        log.error("Ошибка загрузки аватарки: {}", e.getMessage());
        String errorMessage = "Ошибка! Картинка не загрузилась: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateAptekaLoginException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateAptekaLoginException(DublicateAptekaLoginException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaAdressException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaAdressException(InvalidAptekaAdressException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(SelfDeleteException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleSelfDeleteException(SelfDeleteException e) {
        log.warn("Ошибка изменения сотрудников: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения сотрудников: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateGroupTaskException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateGroupTaskException(DublicateGroupTaskException e) {
        log.warn("Группа задачи уже существует: {}", e.getMessage());
        String errorMessage = "Ошибка! Группа задачи уже существует: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateGroupUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateGroupClientException(DublicateGroupUserException e) {
        log.warn("Группа сотрудников уже существует: {}", e.getMessage());
        String errorMessage = "Ошибка! Группа сотрудников уже существует: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleInvalidRefreshTokenException(InvalidRefreshTokenException e) {
        log.warn("Ошибка функционала REFRESH токена: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка функционала REFRESH токена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(GroupUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupUserNotFoundException(GroupUserNotFoundException e) {
        log.error("Группа не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Группа пользователей не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(GroupTaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupTaskNotFoundException(GroupTaskNotFoundException e) {
        log.error("Группа задачи не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Группа задачи не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskNotFoundException(TaskNotFoundException e) {
        log.error("Задача не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Задача не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(TaskPictureNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskPictureNotFoundException(TaskPictureNotFoundException e) {
        log.error("Ошибка при поиске картинки: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка при поиске картинки: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(WorkTypeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleWorkTypeNotFoundException(WorkTypeNotFoundException e) {
        log.error("Ошибка при поиске вида работ: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка при поиске вида работ: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaLoginException(InvalidAptekaLoginException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaPasswordException(InvalidAptekaPasswordException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaNumberException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaNumberException(InvalidAptekaNumberException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaPhoneNumberException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaPhoneNumberException(InvalidAptekaPhoneNumberException e) {
        log.warn("Ошибка изменения аптек: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения аптек: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidGroupTaskException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidGroupTaskException(InvalidGroupTaskException e) {
        log.warn("Группа задачи введена некорректно:", e.getMessage());
        String errorMessage = "Ошибка! Группа задачи введена некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidTaskDescriptionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTaskDescriptionException(InvalidTaskDescriptionException e) {
        log.warn("Ошибка изменения задачи: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения задачи: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidTaskTitleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTaskTitleException(InvalidTaskTitleException e) {
        log.warn("Заголовок задачи введено некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Заголовок задачи введено некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidWorkTypeNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidWorkTypeNameException(InvalidWorkTypeNameException e) {
        log.warn("Ошибка изменения вида работ: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения вида работ: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(TaskCommentNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTaskCommentNotFoundException(TaskCommentNotFoundException e) {
        log.error("Ошибка при поиске комментария {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Ошибка при поиске комментария: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidClientFullNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidClientFullNameException(InvalidClientFullNameException e) {
        log.warn("Ошибка изменения сотрудников: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения сотрудников: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidClientLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidClientLoginException(InvalidClientLoginException e) {
        log.warn("Ошибка изменения сотрудников: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения сотрудников: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidClientPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidClientPasswordException(InvalidClientPasswordException e) {
        log.warn("Ошибка изменения сотрудников: {}", e.getMessage());
        String errorMessage = "Ошибка! Ошибка изменения сотрудников: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleClientNotFoundException(ClientNotFoundException e) {
        log.error("Сотрудник не найден: {}", e.getMessage());
        String errorMessage = "Ошибка! Сотрудник не найден: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Доступ запрещен: {}", e.getMessage());
        String errorMessage = "Ошибка! Доступ запрещен: " + e.getMessage();
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(MethodArgumentNotValidException e) {
        log.error("Ошибка валидации данных: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле '%s': %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining("; "));

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка валидации! " + errorMessage,
                System.currentTimeMillis());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Ошибка валидации параметров запроса: {}", e.getMessage());

        String errorMessage = e.getConstraintViolations().stream()
                .map(violation -> String.format("Параметр '%s': %s", violation.getPropertyPath(),
                        violation.getMessage()))
                .collect(Collectors.joining("; "));

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Некорректные параметры запроса! " + errorMessage,
                System.currentTimeMillis());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingParams(MissingServletRequestParameterException e) {
        log.error("Отсутствует обязательный параметр: {}", e.getParameterName());

        String errorMessage = String.format("Отсутствует обязательный параметр запроса: %s", e.getParameterName());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("Нечитаемый JSON синтаксис: {}", e.getMessage());

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Ошибка синтаксиса JSON. Проверьте структуру отправляемых данных.",
                System.currentTimeMillis());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Нарушение целостности данных в БД: {}", e.getMessage(), e);

        String errorMessage = "Ошибка базы данных: неверный запрос.";

        if (e.getRootCause() instanceof PSQLException psqlException) {
            String sqlState = psqlException.getSQLState();

            switch (sqlState) {
                case "23503" -> errorMessage = "Ошибка внешнего ключа: связанная запись отсутствует или используется.";
                case "23514" -> errorMessage = "Ошибка CHECK ограничения: данные не прошли проверку правил БД.";
                case "23505" -> errorMessage = "Ошибка уникальности: такая запись уже существует.";
            }
        }

        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                errorMessage,
                System.currentTimeMillis());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllUncaughtExceptions(Exception e) {
        log.error("Непредвиденная системная ошибка: {}", e.getMessage(), e);

        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Внутренняя ошибка сервера. Пожалуйста, обратитесь к администратору.",
                System.currentTimeMillis());
    }

}
