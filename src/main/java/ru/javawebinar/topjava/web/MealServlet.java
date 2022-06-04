package ru.javawebinar.topjava.web;

import org.slf4j.Logger;
import ru.javawebinar.topjava.dao.CRUDMealMemory;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.util.MealsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import static org.slf4j.LoggerFactory.getLogger;

public class MealServlet extends HttpServlet {
    private static final Logger log = getLogger(MealServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (Objects.equals(request.getParameter("action"), "create") || Objects.equals(request.getParameter("action"), "update")) {
            log.debug("forward to create");
            request.getRequestDispatcher("/create.jsp").forward(request, response);
        } else if (Objects.equals(request.getParameter("action"), "delete")) {
            log.debug("delete meal");
            new CRUDMealMemory().delete(Integer.parseInt(request.getParameter("id")));
            response.sendRedirect("meals");
        } else {
            log.debug("redirect to meals");
            request.setAttribute("excess", MealsUtil.getExcess());
            request.setAttribute("meals", MealsUtil.filteredByStreams(new CRUDMealMemory().getAll(), LocalTime.MIN, LocalTime.MAX, MealsUtil.getExcess()));
            request.getRequestDispatcher("/meals.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (Objects.equals(request.getParameter("action"), "create")) {
            log.debug("create meal");
            new CRUDMealMemory().create(new Meal(LocalDateTime.parse(request.getParameter("dateTime")), request.getParameter("description"),
                    Integer.parseInt(request.getParameter("calories"))));
        } else if (Objects.equals(request.getParameter("action"), "update")) {
            log.debug("update meal");
            new CRUDMealMemory().update(new Meal(Integer.parseInt(request.getParameter("id")), LocalDateTime.parse(request.getParameter("dateTime")),
                    request.getParameter("description"),
                    Integer.parseInt(request.getParameter("calories"))));
        }
        response.sendRedirect("meals");
    }
}
