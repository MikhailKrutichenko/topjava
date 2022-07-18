package ru.javawebinar.topjava.repository;

import org.slf4j.Logger;
import ru.javawebinar.topjava.model.AbstractBaseEntity;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

public class EntityValidator {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Logger log = getLogger(EntityValidator.class);

    public static void valid(AbstractBaseEntity entity) {
        Set<ConstraintViolation<AbstractBaseEntity>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            violations.forEach(v -> log.info(v.getMessage()));
            throw new ConstraintViolationException(violations);
        }
    }
}
