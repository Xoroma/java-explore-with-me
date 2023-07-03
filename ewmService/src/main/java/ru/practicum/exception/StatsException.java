package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class StatsException extends RuntimeException {
    public StatsException(String message) {
        super(message);
        log.error(message);
    }
}
