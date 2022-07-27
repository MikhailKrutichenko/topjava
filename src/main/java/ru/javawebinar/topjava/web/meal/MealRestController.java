package ru.javawebinar.topjava.web.meal;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.to.MealTo;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping(value = MealRestController.MEAL_REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
public class MealRestController extends AbstractMealController {
    static final String MEAL_REST_URL = "/rest/meals";

    @GetMapping("{id}")
    public Meal get(@PathVariable("id") int id) {
        return super.get(id);
    }

    @GetMapping
    public List<MealTo> getAll() {
        return super.getAll();
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") int id) {
        super.delete(id);
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void update(@RequestBody Meal meal) {
        super.update(meal, meal.id());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Meal> createWithLocation(@RequestBody Meal meal) {
        Meal created = super.create(meal);
        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(MEAL_REST_URL + "/{id}")
                .buildAndExpand("{id}", created.id())
                .toUri();
        return ResponseEntity.created(uri).body(created);
    }

    @GetMapping(value = "/filter")
    public List<MealTo> filter(@RequestParam LocalDate startDate,
                               @RequestParam LocalTime startTime,
                               @RequestParam LocalDate endDate,
                               @RequestParam LocalTime endTime) {

        return super.getBetween(startDate, startTime, endDate, endTime);
    }
}