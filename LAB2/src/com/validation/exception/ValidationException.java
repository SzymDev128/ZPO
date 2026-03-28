package com.validation.exception;
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ValidationException(String errorMessage) {
        super(errorMessage);
    }
    public ValidationException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}