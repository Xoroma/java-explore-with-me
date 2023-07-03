package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class InvalidSortException extends RuntimeException {
    public InvalidSortException(String message) {
        super(message);
        log.error(message);
    }
}
