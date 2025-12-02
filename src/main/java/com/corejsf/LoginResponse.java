package com.corejsf;

/**
 * Data Transfer Object (DTO) returned from the POST /api/login endpoint.
 *
 * When authentication succeeds, the server issues:
 *   • a JWT-style token (or random opaque token)
 *   • basic user information needed by the frontend
 *
 * Example JSON response:
 * <pre>
 * {
 *   "token": "abc123xyz",
 *   "userName": "jdoe",
 *   "name": "John Doe",
 *   "empNumber": 42,
 *   "admin": false
 * }
 * </pre>
 *
 * The frontend stores the token (e.g., in localStorage) and includes it in the
 * Authorization header for all subsequent REST calls:
 *
 *     Authorization: Bearer <token>
 */
public class LoginResponse {

    private String token;
    private String userName;
    private String name;
    private int empNumber;
    private boolean admin;

    public LoginResponse() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getEmpNumber() {
        return empNumber;
    }

    public void setEmpNumber(int empNumber) {
        this.empNumber = empNumber;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
