package com.financial_control.controllers.exceptions;

import java.time.Instant;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.financial_control.services.exceptions.DatabaseException;
import com.financial_control.services.exceptions.ResourceNotFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ResourceExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFound(
            ResourceNotFoundException e,
            HttpServletRequest request) {

        return buildError(
                HttpStatusCode.valueOf(404),
                "Resource not found",
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<StandardError> database(
            DatabaseException e,
            HttpServletRequest request) {

        return buildError(
                HttpStatusCode.valueOf(400),
                "Database exception",
                e.getMessage(),
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validation(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        HttpStatusCode status = HttpStatusCode.valueOf(422);

        ValidationError err = new ValidationError();
        err.setTimestamp(Instant.now());
        err.setStatus(status.value());
        err.setError("Validation exception");
        err.setMessage("One or more fields are invalid");
        err.setPath(request.getRequestURI());

        for (FieldError f : e.getBindingResult().getFieldErrors()) {
            err.addError(f.getField(), f.getDefaultMessage());
        }

        return ResponseEntity.status(status).body(err);
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<StandardError> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {

        return buildError(
                HttpStatusCode.valueOf(400),
                "Malformed JSON",
                "Invalid request body.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<StandardError> buildError(
            HttpStatusCode status,
            String error,
            String message,
            String path) {

        StandardError err = new StandardError();
        err.setTimestamp(Instant.now());
        err.setStatus(status.value());
        err.setError(error);
        err.setMessage(message);
        err.setPath(path);

        return ResponseEntity.status(status).body(err);
    }
}