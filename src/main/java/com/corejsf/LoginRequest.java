package com.corejsf;

/**
 * Data Transfer Object (DTO) representing the JSON payload sent to
 * the POST /api/login endpoint.
 *
 * This class contains only the fields needed for authentication:
 *   • userName  – the user's login name (trimmed before use)
 *   • password  – the user's plaintext password (checked against stored credentials)
 *
 * Example JSON request:
 * <pre>
 * {
 *   "userName": "admin",
 *   "password": "secret"
 * }
 * </pre>
 *
 * A no-argument constructor is required for JSON deserialization by JAX-RS.
 */
public class LoginRequest {

    private String userName;
    private String password;

    /** Default constructor required for JSON frameworks. */
    public LoginRequest() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
