package ru.practicum.explore.exception;

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