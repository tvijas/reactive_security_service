package com.example.reactive.security.utils;

import com.example.reactive.security.exceptions.BasicException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public final class LocalDateTimeParser {

    private LocalDateTimeParser() {
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parse(String string) {
        try {
            return LocalDateTime.parse(string, formatter);
        } catch (Exception ex) {
            throw new BasicException(Map.of("dateTime",
                    "Incorrect date & time format. Must be: yyyy-MM-dd HH:mm:ss"), HttpStatus.BAD_REQUEST);
        }
    }
}
