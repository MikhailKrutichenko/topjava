package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.Crud;
import ru.javawebinar.topjava.dao.MemoryMealCrud;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);
    private static final Crud<Meal> crud = new MemoryMealCrud();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        action = action == null ? "meals" : action;
        switch (action) {
            case ("edit"):
                log.debug("forward to create");
                String id = request.getParameter("id");
                Meal meal;
                if (id != null) {
                    meal = crud.getById(Integer.parseInt(id));
                } else {
                    meal = new Meal(LocalDateTime.now(Clock.tickMinutes(ZoneId.systemDefault())), null, 0);
                }
                request.setAttribute("meal", meal);
                request.getRequestDispatcher("/editMeals.jsp").forward(request, response);
                break;
            case ("delete"):
                log.debug("delete meal");
                crud.delete(Integer.parseInt(request.getParameter("id")));
                response.sendRedirect("meals");
                break;
            case ("meals"):
                log.debug("redirect to meals");
                request.setAttribute("dateTimeFormatter", DATE_TIME_FORMATTER);
                request.setAttribute("meals", MealsUtil.filteredByStreams(crud.getAll(),
                        LocalTime.MIN, LocalTime.MAX, MealsUtil.NORM_CALORIES));
                request.getRequestDispatcher("/meals.jsp").forward(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String id = request.getParameter("id");
        Meal meal = new Meal(LocalDateTime.parse(request.getParameter("dateTime")), request.getParameter("description"),
                Integer.parseInt(request.getParameter("calories")));
        if (id.isEmpty()) {
            log.debug("create meal");
            crud.create(meal);
        } else {
            log.debug("update meal");
            meal.setId(Integer.parseInt(id));
            crud.update(meal);
        }
        response.sendRedirect("meals");
    }
}
