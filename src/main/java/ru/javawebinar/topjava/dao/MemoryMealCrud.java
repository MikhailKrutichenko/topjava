package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryMealCrud implements Crud<Meal> {
    private final AtomicInteger idCounter = new AtomicInteger();
    private final Map<Integer, Meal> meals = new ConcurrentHashMap<>();

    {
        MealsUtil.getMeals().forEach(this::create);
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
    public Meal update(Meal entity) {
        return meals.replace(entity.getId(), entity) == null ? null : entity;
    }

    @Override
    public boolean delete(int id) {
        return meals.remove(id) != null;
    }

    @Override
    public Meal create(Meal entity) {
        entity.setId(idCounter.getAndIncrement());
        meals.put(entity.getId(), entity);
        return entity;
    }
}
