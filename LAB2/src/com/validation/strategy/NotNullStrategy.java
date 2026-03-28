package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;

import com.validation.annotation.NotNull;
public class NotNullStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(NotNull.class) && value == null) {
            NotNull annotation = field.getAnnotation(NotNull.class);
            String errorInfo = String.format("Pole %s: %s", field.getName(), annotation.message());
            return Optional.of(errorInfo);
        }
        return Optional.empty();
    }
}