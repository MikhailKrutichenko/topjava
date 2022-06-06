package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryMealCrud implements CRUD<Meal> {

    private final AtomicInteger idCounter = new AtomicInteger();
    private final Map<Integer, Meal> meals = new ConcurrentHashMap<>();

    {
        for (Meal meal : MealsUtil.getMeals()) {
            meal.setId(idCounter.getAndIncrement());
            create(meal);
        }
    }

    @Override
    public List<Meal> getAll() {
        return new ArrayList<>(meals.values());
    }

    @Override
    public Meal getById(int id) {
        return meals.get(id);
    }

    @Override
    public Meal update(int id, Meal entity) {
        entity.setId(id);
        if (meals.replace(id, entity) != null) {
            return entity;
        }
        return null;
    }

    @Override
    public boolean delete(int id) {
        return meals.remove(id) != null;
    }

    @Override
    public Meal create(Meal entity) {
        entity.setId(idCounter.getAndIncrement());
        return meals.put(entity.getId(), entity);
    }
}
