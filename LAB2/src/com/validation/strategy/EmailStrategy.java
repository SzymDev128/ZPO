package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.regex.Pattern;

import com.validation.annotation.Email;

public class EmailStrategy implements ValidationStrategy {
	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

	@Override
	public Optional<String> validate(Field field, Object value) {
		if (field.isAnnotationPresent(Email.class)
				&& value != null
				&& (!String.class.equals(value.getClass())
				|| !EMAIL_PATTERN.matcher((String) value).matches())) {
			Email annotation = field.getAnnotation(Email.class);
			String errorInfo = String.format("Pole %s: %s", field.getName(), annotation.message());
			return Optional.of(errorInfo);
		}
		return Optional.empty();
	}
}
