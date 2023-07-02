package ru.practicum.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionController {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(Throwable e) {
        log.debug("Получен статус 500 INTERNAL SERVER ERROR {}", e.getMessage(), e);
        return new ApiError("An unexpected error has occurred.", "The required object was not found.", HttpStatus.INTERNAL_SERVER_ERROR, List.of());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIncorrectValidationException(ValidationException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "The required object was not found.", HttpStatus.BAD_REQUEST, List.of());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbiddenException(ForbiddenException e) {
        log.debug("Получен статус 403 Forbidden {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "The required object was forbidden.", HttpStatus.FORBIDDEN, List.of());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.debug("Получен статус 404 Not found {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "The required object was not found.", HttpStatus.NOT_FOUND, List.of());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleIncorrectValidationException(ConflictException e) {
        log.debug("Получен статус 409 Conflict {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "The required object has conflict.", HttpStatus.CONFLICT, List.of());
    }

    @ExceptionHandler(ConflictValueException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIncorrectConflictValueException(ConflictValueException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "The required object was not found.", HttpStatus.BAD_REQUEST, List.of());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError methodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "An unexpected error has occurred.", HttpStatus.BAD_REQUEST, List.of());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiError missingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "An unexpected error has occurred.", HttpStatus.BAD_REQUEST, List.of());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiError methodPositiveOrZeroValid(ConstraintViolationException e) {
        log.debug("Получен статус 400 Bad request {}", e.getMessage(), e);
        return new ApiError(e.getMessage(), "An unexpected error has occurred.", HttpStatus.BAD_REQUEST, List.of());
    }
}