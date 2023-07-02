package ru.practicum;

import java.time.format.DateTimeFormatter;

public class Constants {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String APP_NAME = "ewm-main-service";
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final String START = "2010-01-01 00:00:00";
    public static final String END = "2095-01-01 00:00:00";
}
