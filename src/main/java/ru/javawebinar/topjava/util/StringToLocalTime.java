package ru.javawebinar.topjava.util;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class StringToLocalTime implements Converter<String, LocalTime> {
    @Override
    public LocalTime convert(String source) {
        if(source == null) {
            return null;
        }
        return LocalTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE);
    }
}
