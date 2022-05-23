package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
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
    }

    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO return filtered list with excess. Implement by cycles
        Map<LocalDate, Integer> tempMap = new HashMap<>();
        for (UserMeal meal : meals) {
            LocalDate currentDate = meal.getDateTime().toLocalDate();
            tempMap.merge(currentDate, meal.getCalories(), Integer::sum);
        }
        List<UserMealWithExcess> mealsWithExcess = new ArrayList<>();
        for (UserMeal meal : meals) {
            if (TimeUtil.isBetweenHalfOpen(meal.getDateTime().toLocalTime(), startTime, endTime)) {
                boolean excess = tempMap.get(meal.getDateTime().toLocalDate()) > caloriesPerDay;
                mealsWithExcess.add(new UserMealWithExcess(meal.getDateTime(), meal.getDescription(), meal.getCalories(), excess));
            }
        }
        return mealsWithExcess;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        // TODO Implement by streams
        Map<LocalDate, Integer> tempMap = meals.stream()
                .collect(Collectors.toMap((k -> k.getDateTime().toLocalDate()), UserMeal::getCalories, Integer::sum));
        return meals.stream()
                .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime))
                .map(m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
                        tempMap.get(m.getDateTime().toLocalDate()) > caloriesPerDay))
                .collect(Collectors.toList());
    }


    public static List<UserMealWithExcess> filterByStreamsOptional2(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        class Store {
            private Integer sumCalories;
            private List<UserMeal> userMealsPerDay = new ArrayList<>();

            public Store(UserMeal userMeal) {
                this.sumCalories = userMeal.getCalories();
                userMealsPerDay.add(userMeal);
            }

            public Store addUserMeals(Store store) {
                sumCalories += store.getSumCalories();
                userMealsPerDay.addAll(store.getUserMealsPerDay());
                return this;
            }

            public Integer getSumCalories() {
                return sumCalories;
            }

            public List<UserMeal> getUserMealsPerDay() {
                return userMealsPerDay;
            }
        }
        return meals.stream()
                .collect(new Collector<UserMeal, Map<LocalDate, Store>, List<UserMealWithExcess>>() {
                    @Override
                    public Supplier<Map<LocalDate, Store>> supplier() {
                        return HashMap::new;
                    }

                    @Override
                    public BiConsumer<Map<LocalDate, Store>, UserMeal> accumulator() {
                        return (map, um) -> map.merge(um.getDateTime().toLocalDate(), new Store(um),
                                (newStore, oldStore) -> oldStore.addUserMeals(newStore));
                    }

                    @Override
                    public BinaryOperator<Map<LocalDate, Store>> combiner() {
                        return (f, s) -> Stream.concat(f.entrySet().stream(), s.entrySet().stream())
                                .collect(Collectors.toMap(Map.Entry::getKey,
                                        Map.Entry::getValue, (newV, oldV) -> oldV.addUserMeals(newV)));
                    }

                    @Override
                    public Function<Map<LocalDate, Store>, List<UserMealWithExcess>> finisher() {
                        return map -> map.values().stream()
                                .flatMap(s -> s.getUserMealsPerDay().stream()
                                        .map(m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
                                                s.sumCalories > caloriesPerDay))
                                        .filter(m -> TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(), startTime, endTime)))
                                .collect(Collectors.toList());
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        return EnumSet.of(Characteristics.CONCURRENT);
                    }
                });
    }
}