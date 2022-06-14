package ru.javawebinar.topjava.repository.inmemory;

import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.DateTimeUtil;
import ru.javawebinar.topjava.util.MealsUtil;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private final Map<Integer, Map<Integer, Meal>> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        int count = 0;
        int id = 1;
        for (Meal meal : MealsUtil.meals) {
            if (count++ == 7) {
                id = 2;
            }
            this.save(meal, id);
        }
    }

    @Override
    public Meal save(Meal meal, int userId) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            repository.putIfAbsent(userId, new ConcurrentHashMap<>());
            repository.get(userId).put(meal.getId(), meal);
            return meal;
        }
        Meal updateMeal = get(meal.getId(), userId);
        if (updateMeal == null) {
            return null;
        }
        return repository.get(userId).computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
    }

    @Override
    public boolean delete(int id, int userId) {
        Map<Integer, Meal> meals = repository.get(userId);
        return meals != null && meals.remove(id) != null;
    }

    @Override
    public Meal get(int id, int userId) {
        Map<Integer, Meal> meals = repository.get(userId);
        if (meals == null) {
            return null;
        }
        return meals.get(id);
    }

    @Override
    public List<Meal> getAllByFilter(int userId, LocalDate startDate, LocalDate endDate) {
        return filterByPredicate(repository.get(userId), m -> DateTimeUtil.isBetweenInclusive(m.getDate(), startDate, endDate));
    }

    @Override
    public List<Meal> getAll(int userId) {
        return filterByPredicate(repository.get(userId), m -> true);
    }

    private List<Meal> filterByPredicate(Map<Integer, Meal> meals, Predicate<Meal> filter) {
        if (meals != null) {
            return meals.values().stream()
                    .filter(filter)
                    .sorted(Comparator.comparing(Meal::getDateTime).reversed())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}

