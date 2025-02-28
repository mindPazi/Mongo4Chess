package com.example.demo.controller;

import com.example.demo.DTO.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler is a class that handles exceptions globally across the application.
 * It uses @ControllerAdvice to apply to all controllers.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles MethodArgumentNotValidException exceptions.
     * This method is triggered when a validation error occurs.
     *
     * @param ex the MethodArgumentNotValidException exception
     * @return a ResponseEntity containing a list of ValidationErrorResponse objects and a BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ValidationErrorResponse>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<ValidationErrorResponse> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> new ValidationErrorResponse(((FieldError) error).getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}