package ru.javawebinar.topjava.dao;

import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CRUDMealMemory implements CRUD<Meal> {
    private static final ConcurrentHashMap<Integer, Meal> meals;

    static {
        meals = new ConcurrentHashMap<>();
        for (Meal meal : MealsUtil.getMeals()) {
            meals.put(meal.getId(), meal);
        }
    }

    @Override
    public List<Meal> getAll() {
        return new ArrayList<>(meals.values());
    }

    @Override
    public Meal getById(Integer id) {
        return meals.get(id);
    }

    @Override
    public Meal update(Meal entity) {
        return meals.replace(entity.getId(), entity);
    }

    @Override
    public Meal delete(Integer id) {
        return meals.remove(id);
    }

    @Override
    public Meal create(Meal entity) {
        return meals.put(entity.getId(), entity);
    }
}
