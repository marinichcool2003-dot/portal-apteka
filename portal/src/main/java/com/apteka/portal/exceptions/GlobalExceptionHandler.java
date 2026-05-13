package com.apteka.portal.exceptions;

import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CompletionException.class)
    public ErrorResponse handleCompletionException(CompletionException e) {
        Throwable cause = e.getCause();
        log.error("Асинхронная ошибка: {}", cause.getMessage(), cause);

        if (cause instanceof AptekaNotFoundException ex) {
            return handleAptekaNotFoundException(ex);
        }
        if (cause instanceof AptekaCreateException ex) {
            return handleAptekaCreateException(ex);
        }
        if (cause instanceof DublicateAptekaLoginException ex) {
            return handleDublicateAptekaLoginException(ex);
        }
        if (cause instanceof DublicateGroupTaskException ex) {
            return handleDublicateGroupTaskException(ex);
        }
        if (cause instanceof GroupAptekiNotFoundException ex) {
            return handleGroupAptekiNotFoundException(ex);
        }
        if (cause instanceof GroupTaskNotFoundException ex) {
            return handleGroupTaskNotFoundException(ex);
        }
        if (cause instanceof TaskNotFoundException ex) {
            return handleTaskNotFoundException(ex);
        }
        if (cause instanceof InvalidAptekaLoginException ex) {
            return handleInvalidAptekaLoginException(ex);
        }
        if (cause instanceof InvalidAptekaPasswordException ex) {
            return handleInvalidAptekaPasswordException(ex);
        }
        if (cause instanceof InvalidGroupAptekiException ex) {
            return handleInvalidGroupAptekiException(ex);
        }
        if (cause instanceof InvalidGroupTaskException ex) {
            return handleInvalidGroupTaskException(ex);
        }
        if (cause instanceof InvalidTaskDescriptionException ex) {
            return handleInvalidTaskDescriptionException(ex);
        }
        if (cause instanceof InvalidTaskTitleException ex) {
            return handleInvalidTaskTitleException(ex);
        }

        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Неизвестная ошибка при асинхронном выполнении задачи: " + cause.getMessage(),
                System.currentTimeMillis());
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIOException(IOException e) {
        log.error("Ошибка загрузки аватарки", e);
        String errorMessage = "Ошибка! Картинка не загрузилась: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Ошибка валидации при регистрации аптеки", e);
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

    @ExceptionHandler(AptekaCreateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleAptekaCreateException(AptekaCreateException e) {
        log.error("Аптека не создана: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Аптека не создана: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(DublicateAptekaLoginException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDublicateAptekaLoginException(DublicateAptekaLoginException e) {
        log.warn("Аптека уже существует: {}", e.getMessage());
        String errorMessage = "Ошибка! Аптека уже существует: " + e.getMessage();
        return new ErrorResponse(HttpStatus.CONFLICT.value(), errorMessage, System.currentTimeMillis());
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

    @ExceptionHandler(GroupAptekiNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupAptekiNotFoundException(GroupAptekiNotFoundException e) {
        log.error("Группа аптеки не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Группа аптеки не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(GroupUserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGroupClientNotFoundException(GroupUserNotFoundException e){
        log.error("Группа не найдена: {}", e.getMessage(), e);
        String errorMessage = "Ошибка! Группа сотрудников не найдена: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage,System.currentTimeMillis());
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

    @ExceptionHandler(InvalidAptekaLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaLoginException(InvalidAptekaLoginException e) {
        log.warn("Логин введён некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Логин введён некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidAptekaPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAptekaPasswordException(InvalidAptekaPasswordException e) {
        log.warn("Пароль введён некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Пароль введён некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidGroupAptekiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidGroupAptekiException(InvalidGroupAptekiException e) {
        log.warn("Группа аптеки введена некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Группа аптеки введена некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidGroupClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidGroupClientException(InvalidGroupClientException e) {
        log.warn("Группа введена некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Группа сотрудников введена некорректно: " + e.getMessage();
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
        log.warn("Описание задачи введено некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Описание задачи введено некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidTaskTitleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidTaskTitleException(InvalidTaskTitleException e) {
        log.warn("Заголовок задачи введено некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Заголовок задачи введено некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidClientLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidClientLoginException(InvalidClientLoginException e){
        log.warn("Логин сотрудника введён некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Логин сотрудника введён некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(InvalidClientPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidClientPasswordException(InvalidClientPasswordException e){
        log.warn("Пароль сотрудника введён некорректно: {}", e.getMessage());
        String errorMessage = "Ошибка! Пароль сотрудника введён некорректно: " + e.getMessage();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errorMessage, System.currentTimeMillis());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleClientNotFoundException(ClientNotFoundException e){
        log.error("Сотрудник не найден: {}", e.getMessage());
        String errorMessage = "Ошибка! Сотрудник не найден: " + e.getMessage();
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), errorMessage, System.currentTimeMillis());
    }

}
