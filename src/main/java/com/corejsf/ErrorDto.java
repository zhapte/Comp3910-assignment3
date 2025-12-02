package com.corejsf;

/**
 * Simple Data Transfer Object representing an error message returned
 * by REST endpoints.
 *
 * This class provides a consistent JSON structure for all error responses.
 *
 * Example JSON output:
 * <pre>
 * {
 *   "message": "Invalid username or password"
 * }
 * </pre>
 *
 * Typical usage scenarios:
 *   - Validation failures (400 Bad Request)
 *   - Unauthorized or forbidden actions (401 / 403)
 *   - Missing resources (404 Not Found)
 *   - Server-side errors (500 Internal Server Error)
 */
public class ErrorDto {

    private String message;

    /** Default no-arg constructor required for JSON serialization/deserialization. */

    public ErrorDto() {
    }

    /**
     * Convenience constructor for quickly creating an error response.
     *
     * @param message the error message to return
     */
    public ErrorDto(String message) {
        this.message = message;
    }

    /**
     * getter 
     * @return the error message string 
     */
    public String getMessage() {
        return message;
    }

    /**
     * setter
     *  @param message new value for the error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
