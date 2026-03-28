package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;

import com.validation.annotation.NotEmpty;

public class NotEmptyStrategy implements ValidationStrategy {
	@Override
	public Optional<String> validate(Field field, Object value) {
		if (field.isAnnotationPresent(NotEmpty.class)
				&& (value == null || String.valueOf(value).trim().isEmpty())) {
			NotEmpty annotation = field.getAnnotation(NotEmpty.class);
			String errorInfo = String.format("Pole %s: %s", field.getName(), annotation.message());
			return Optional.of(errorInfo);
		}
		return Optional.empty();
	}
}
