package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OperationFailedException extends RuntimeException {
    public OperationFailedException(String message) {
        super(message);
        log.error(message);
    }
}
