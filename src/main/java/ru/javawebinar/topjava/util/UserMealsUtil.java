package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 222),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );
        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);
        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
        System.out.println(filterByStreamsOptional2(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> dailyCalories = new HashMap<>();
        for (UserMeal meal : meals) {
            LocalDate currentMealDate = meal.getDateTime().toLocalDate();
            dailyCalories.merge(currentMealDate, meal.getCalories(), Integer::sum);
        }
        List<UserMealWithExcess> mealsWithExcess = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                boolean excess = dailyCalories.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                mealsWithExcess.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));
            }
        }
        return mealsWithExcess;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate, Integer> dailyCalories = meals.stream()
                .collect(Collectors.toMap((m -> m.getDateTime().toLocalDate()), UserMeal::getCalories, Integer::sum));
        return meals.stream()
                .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                .map(m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
                        dailyCalories.get(m.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }


    public static List<UserMealWithExcess> filterByStreamsOptional2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        class DailyMeals {
            private Integer sumCalories;
            private List<UserMeal> userMeals = new ArrayList<>();

            public DailyMeals(UserMeal userMeal) {
                this.sumCalories = userMeal.getCalories();
                userMeals.add(userMeal);
            }

            public DailyMeals combine(DailyMeals dailyMeals) {
                sumCalories += dailyMeals.getSumCalories();
                userMeals.addAll(dailyMeals.getUserMeals());
                return this;
            }

            public Integer getSumCalories() {
                return sumCalories;
            }

            public List<UserMeal> getUserMeals() {
                return userMeals;
            }
        }
        return meals.stream()
                .collect(new Collector<UserMeal, Map<LocalDate, DailyMeals>, List<UserMealWithExcess>>() {
                    @Override
                    public Supplier<Map<LocalDate, DailyMeals>> supplier() {
                        return ConcurrentHashMap::new;
                    }

                    @Override
                    public BiConsumer<Map<LocalDate, DailyMeals>, UserMeal> accumulator() {
                        return (map, um) -> map.merge(um.getDateTime().toLocalDate(), new DailyMeals(um),
                                (newStore, oldStore) -> oldStore.combine(newStore));
                    }

                    @Override
                    public BinaryOperator<Map<LocalDate, DailyMeals>> combiner() {
                        return (f, s) -> Stream.concat(f.entrySet().stream(), s.entrySet().stream())
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        Map.Entry::getValue, (newV, oldV) -> oldV.combine(newV)));
                    }

                    @Override
                    public Function<Map<LocalDate, DailyMeals>, List<UserMealWithExcess>> finisher() {
                        return map -> map.values().stream()
                                .flatMap(s -> s.getUserMeals().stream()
                                        .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                                        .map(m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
                                                s.sumCalories > caloriesPerDay)))
                                .collect(Collectors.toList());
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        return EnumSet.of(Characteristics.CONCURRENT);
                    }
                });
    }
}