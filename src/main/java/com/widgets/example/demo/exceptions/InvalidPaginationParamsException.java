package com.widgets.example.demo.exceptions;

public class InvalidPaginationParamsException extends Exception {
    public InvalidPaginationParamsException(String errorMessage) {
        super(errorMessage);
    }
}
