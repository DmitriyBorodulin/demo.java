package com.widgets.example.demo.exceptions;

public class InvalidFilterParamsException extends Exception {
    public InvalidFilterParamsException(String errorMessage) {
        super(errorMessage);
    }
}