package com.corejsf;

/**
 * Standardized error body for REST responses.
 *
 * Example:
 * {
 *   "message": "Invalid username or password"
 * }
 */
public class ErrorDto {

    private String message;

    public ErrorDto() {
    }

    public ErrorDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
