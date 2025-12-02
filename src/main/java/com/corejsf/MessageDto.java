package com.corejsf;

/**
 * Simple Data Transfer Object (DTO) used to return generic messages
 * from REST endpoints.
 *
 * This is typically used for non-error informational responses such as:
 *   • logout confirmation
 *   • status messages
 *   • simple acknowledgements (e.g., "Operation completed")
 *
 * Example JSON output:
 * <pre>
 * {
 *   "message": "Logged out successfully"
 * }
 * </pre>
 *
 * This class mirrors ErrorDto but is used for *successful* operations.
 */
public class MessageDto {
    private String message;

    public MessageDto() {}
    public MessageDto(String message) { this.message = message; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}