package ru.practicum.exceptions;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static ru.practicum.Constants.DATE_FORMAT;

@Data
public class ApiError {
    private List<String> errors;
    private String message;
    private String status;
    private String reason;
    private String timestamp;

    public ApiError(String message, String reason, HttpStatus status, List<String> errors) {
        this.message = message;
        this.reason = reason;
        this.status = status.name();
        this.errors = errors;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        timestamp = LocalDateTime.now().format(formatter);
    }
}
