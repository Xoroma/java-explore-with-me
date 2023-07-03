package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WrongFieldValueException extends RuntimeException {
    public WrongFieldValueException(String message) {
        super(message);
        log.error(message);
    }

}
