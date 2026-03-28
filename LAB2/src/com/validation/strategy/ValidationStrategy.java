package com.validation.strategy;
import java.lang.reflect.Field;
import java.util.Optional;

public interface ValidationStrategy {
    Optional<String> validate(Field field, Object value);
}
