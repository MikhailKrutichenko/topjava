package ru.javawebinar.topjava.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import ru.javawebinar.topjava.MealTestData;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;

import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.ADMIN_ID;
import static ru.javawebinar.topjava.UserTestData.USER_ID;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
        "classpath:spring/spring-app-jdbc.xml",
        "classpath:spring/spring-db.xml"
})
@RunWith(SpringRunner.class)
@Sql(scripts = "classpath:db/populateDB.sql", config = @SqlConfig(encoding = "UTF-8"))
public class MealServiceTest {

    static {
        SLF4JBridgeHandler.install();
    }

    @Autowired
    private MealService service;

    @Test
    public void get() {
        Meal actualMeal = service.get(USER_MEAL_1_ID, USER_ID);
        assertMatch(actualMeal, mealUser1);
    }

    @Test
    public void getNotFound() {
        Assert.assertThrows(NotFoundException.class, () -> service.get(NOT_EXIST_MEAL_ID, USER_ID));
    }

    @Test
    public void getForeign() {
        Assert.assertThrows(NotFoundException.class, () -> service.get(ADMIN_MEAL_3_ID, USER_ID));
    }

    @Test
    public void delete() {
        service.delete(USER_MEAL_1_ID, USER_ID);
        assertThrows(NotFoundException.class, () -> service.get(USER_MEAL_1_ID, USER_ID));
    }

    @Test
    public void deleteForeign() {
        assertThrows(NotFoundException.class, () -> service.delete(ADMIN_MEAL_3_ID, USER_ID));
    }

    @Test
    public void deleteNotExist() {
        assertThrows(NotFoundException.class, () -> service.delete(NOT_EXIST_MEAL_ID, USER_ID));
    }

    @Test
    public void getBetweenInclusive() {
        assertMatch(service.getBetweenInclusive(LocalDate.of(2020, Month.JANUARY, 30),
                LocalDate.of(2020, Month.JANUARY, 30), USER_ID), Arrays.asList(mealUser3, mealUser2, mealUser1));
    }

    @Test
    public void getBetweenInclusiveDateTimeNull() {
        assertMatch(service.getBetweenInclusive(null, null, USER_ID),
                Arrays.asList(mealUser4, mealUser3, mealUser2, mealUser1));
    }

    @Test
    public void getAll() {
        assertMatch(service.getAll(ADMIN_ID), Arrays.asList(mealAdmin4, mealAdmin3, mealAdmin2, mealAdmin1));
    }

    @Test
    public void update() {
        service.update(MealTestData.getUpdated(), USER_ID);
        assertMatch(service.get(USER_MEAL_1_ID, USER_ID), MealTestData.getUpdated());
    }

    @Test
    public void updateForeign() {
        assertThrows(NotFoundException.class, () -> service.update(MealTestData.getUpdated(), ADMIN_ID));
    }

    @Test
    public void create() {
        Meal createMeal = service.create(MealTestData.getNew(), USER_ID);
        Integer mealId = createMeal.getId();
        Meal expectedMeal = MealTestData.getNew();
        expectedMeal.setId(mealId);
        assertMatch(createMeal, expectedMeal);
        assertMatch(service.get(mealId, USER_ID), expectedMeal);
    }

    @Test
    public void duplicateDateTimeCreate() {
        Meal createMeal = MealTestData.getNew();
        createMeal.setDateTime(service.get(USER_MEAL_1_ID, USER_ID).getDateTime());
        assertThrows(DuplicateKeyException.class, () -> service.create(createMeal, USER_ID));
    }
}