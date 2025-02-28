package com.example.demo.DTO;

import lombok.Data;

@Data
public class ValidationErrorResponse {
    private String field;
    private String errorMessage;

    public ValidationErrorResponse(String field, String errorMessage) {
        this.field = field;
        this.errorMessage = errorMessage;
    }

}