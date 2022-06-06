package ru.javawebinar.topjava.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Meal {

    private Integer id;
    private final LocalDateTime dateTime;
    private final String description;
    private final Integer calories;

    public Meal(LocalDateTime dateTime, String description, Integer calories) {
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    public Integer getCalories() {return calories;}

    public LocalDate getDate() {
        return dateTime.toLocalDate();
    }

    public LocalTime getTime() {
        return dateTime.toLocalTime();
    }
}
