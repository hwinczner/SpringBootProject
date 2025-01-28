package com.SpringBoot.Project.Models;

import java.security.PublicKey;
import java.util.List;

public class Result<T>{

    private boolean success;     // Indicates if the operation was successful
    private T data;              // Contains the response data if applicable
    private String message;      // A user-friendly message about the operation
    private List<String> errors; // A list of error messages, if any

    public Result(boolean success, T data, String message, List<String> errors) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errors = errors;
    }

    // Static factory methods for convenience
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, data, message, null);
    }

    public static <T> Result<T> failure(String message, List<String> errors) {
        return new Result<>(false, null, message, errors);
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getErrors() {
        return errors;
    }
}
