package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.CRUD;
import ru.javawebinar.topjava.dao.MemoryMealCrud;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {

    private static final Logger log = getLogger(MealServlet.class);
    private static final CRUD crud = new MemoryMealCrud();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
                    meal = (Meal) crud.getById(Integer.parseInt(id));
                } else {
                    meal = new Meal(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), null, null);
                }
                System.out.println(meal);
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
                request.setAttribute("dateTimeFormatter", dateTimeFormatter);
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
        if (id.isEmpty()) {
            log.debug("create meal");
            crud.create(new Meal(LocalDateTime.parse(request.getParameter("dateTime")), request.getParameter("description"),
                    Integer.parseInt(request.getParameter("calories"))));
        } else {
            log.debug("update meal");
            crud.update(Integer.parseInt(id), new Meal(LocalDateTime.parse(request.getParameter("dateTime")),
                    request.getParameter("description"),
                    Integer.parseInt(request.getParameter("calories"))));
        }
        response.sendRedirect("meals");
    }
}
