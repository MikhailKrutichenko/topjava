package ru.javawebinar.topjava.service.user;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import ru.javawebinar.topjava.Profiles;
import ru.javawebinar.topjava.UserTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.model.User;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.USER_MATCHER;
import static ru.javawebinar.topjava.UserTestData.guest;

@ActiveProfiles(Profiles.DATAJPA)
public class DataJpaUserServiceTest extends AbstractUserServiceTest {

    @Test
    public void getWithMeals() {
        User expectedUser = UserTestData.user;
        List<Meal> expectedMeals = Arrays.asList(meal7, meal6, meal5, meal4, meal3, meal2, meal1);
        User actualUser = service.getWithMeals(expectedUser.id());
        USER_MATCHER.assertMatch(actualUser, expectedUser);
        MEAL_MATCHER.assertMatch(actualUser.getMeals(), expectedMeals);
    }

    @Test
    public void getWithMealNotFound() {
        Assert.assertThrows(NotFoundException.class, () -> service.getWithMeals(NOT_FOUND));
    }

    @Test
    public void getWithoutMeals() {
        User expectedUser = guest;
        expectedUser.setMeals(Collections.emptyList());
        User actualUser = service.getWithMeals(guest.id());
        USER_MATCHER.assertMatch(actualUser, expectedUser);
        MEAL_MATCHER.assertMatch(actualUser.getMeals(), expectedUser.getMeals());
    }
}
