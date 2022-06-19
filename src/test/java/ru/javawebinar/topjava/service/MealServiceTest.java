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
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.exception.NotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ru.javawebinar.topjava.MealTestData.*;
import static ru.javawebinar.topjava.UserTestData.USER_ID;
import static ru.javawebinar.topjava.model.AbstractBaseEntity.START_SEQ;

@ContextConfiguration({
        "classpath:spring/spring-app.xml",
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
        Meal expectedMeal = service.get(MEAL_1_ID, START_SEQ);
        Assert.assertEquals(MEAL_1, expectedMeal);
    }

    @Test
    public void getNotFound() {
        Assert.assertThrows(NotFoundException.class, () -> service.get(NOT_FOUND, START_SEQ));
    }

    @Test
    public void getForeign() {
        Assert.assertThrows(NotFoundException.class, () -> service.get(MEAL_7_ID, START_SEQ));
    }

    @Test
    public void delete() {
        service.delete(MEAL_1_ID, USER_ID);
        assertThrows(NotFoundException.class, () -> service.get(MEAL_1_ID, USER_ID));
    }

    @Test
    public void deleteForeign() {
        assertThrows(NotFoundException.class, () -> service.delete(MEAL_7_ID, USER_ID));
    }

    @Test
    public void getBetweenInclusive() {
        assertEquals(service.getBetweenInclusive(LocalDate.parse("2020-01-30"), LocalDate.parse("2020-01-30"), USER_ID),
                Arrays.asList(MEAL_3, MEAL_2, MEAL_1));
    }

    @Test
    public void getAll() {
        assertEquals(service.getAll(ADMIN_ID), Arrays.asList(MEAL_7, MEAL_6, MEAL_5, MEAL_4));
    }

    @Test
    public void update() {
        service.update(getUpdated(), USER_ID);
        assertEquals(service.get(MEAL_1_ID, USER_ID), getUpdated());
    }

    @Test
    public void updateForeign() {
        assertThrows(NotFoundException.class, () -> service.update(getUpdated(), ADMIN_ID));
    }

    @Test
    public void create() {
        Meal createMeal = service.create(getNew(), USER_ID);
        Integer mealId = createMeal.getId();
        Meal expectedMeal = getNew();
        expectedMeal.setId(mealId);
        assertEquals(createMeal, expectedMeal);
        assertEquals(service.get(mealId, USER_ID), expectedMeal);
    }

    @Test
    public void duplicateTimeCreate() {
        Meal createMeal = getNew();
        createMeal.setDateTime(LocalDateTime.parse("2020-01-30T10:00"));
        System.out.println(createMeal);
        assertThrows(DuplicateKeyException.class, () -> service.create(createMeal, USER_ID));
    }
}