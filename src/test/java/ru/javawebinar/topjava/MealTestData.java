package ru.javawebinar.topjava;

import ru.javawebinar.topjava.model.Meal;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.javawebinar.topjava.model.AbstractBaseEntity.START_SEQ;

public class MealTestData {
    public static final int MEAL_USER_1_ID = START_SEQ + 3;
    public static final int MEAL_USER_2_ID = START_SEQ + 4;
    public static final int MEAL_USER_3_ID = START_SEQ + 5;
    public static final int MEAL_USER_4_ID = START_SEQ + 6;
    public static final int MEAL_ADMIN_5_ID = START_SEQ + 7;
    public static final int MEAL_ADMIN_6_ID = START_SEQ + 8;
    public static final int MEAL_ADMIN_7_ID = START_SEQ + 9;
    public static final int MEAL_ADMIN_8_ID = START_SEQ + 10;

    public static final Meal meal_user_1 = new Meal(MEAL_USER_1_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500);
    public static final Meal meal_user_2 = new Meal(MEAL_USER_2_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000);
    public static final Meal meal_user_3 = new Meal(MEAL_USER_3_ID, LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500);
    public static final Meal meal_user_4 = new Meal(MEAL_USER_4_ID, LocalDateTime.of(2020, Month.FEBRUARY, 25, 20, 0), "Ужин", 500);
    public static final Meal meal_admin_5 = new Meal(MEAL_ADMIN_5_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение Admin", 100);
    public static final Meal meal_admin_6 = new Meal(MEAL_ADMIN_6_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак Admin", 1000);
    public static final Meal meal_admin_7 = new Meal(MEAL_ADMIN_7_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед Admin", 500);
    public static final Meal meal_admin_8 = new Meal(MEAL_ADMIN_8_ID, LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин Admin", 410);

    public static Meal getNew() {
        return new Meal(null, LocalDateTime.MIN, "Lunch", 909);
    }

    public static Meal getUpdated() {
        Meal meal = new Meal(meal_user_1);
        meal.setCalories(250);
        meal.setDateTime(LocalDateTime.MAX);
        meal.setDescription("Update");
        return meal;
    }

    public static void assertMatch(Meal expected, Meal actual) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

    public static void assertMatch(Iterable<Meal> expected, Iterable<Meal> actual) {
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }

}
