package com.corejsf;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import ca.bcit.infosys.employee.Employee;

/**
 * Simple in-memory token store.
 *
 * Maps opaque token strings to authenticated Employee objects.
 * In a real system you'd persist tokens or use JWTs, but this
 * is sufficient for the assignment.
 */
@ApplicationScoped
public class AuthTokenStore {

    private final Map<String, Employee> tokens = new ConcurrentHashMap<>();

    /**
     * Issues a new token for the given employee and stores the mapping.
     *
     * @param employee authenticated employee
     * @return generated opaque token string
     */
    public String issueToken(Employee employee) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, employee);
        return token;
    }

    /**
     * Returns the Employee associated with the given token, or null if invalid.
     */
    public Employee getEmployeeForToken(String token) {
        if (token == null) {
            return null;
        }
        return tokens.get(token);
    }

    /**
     * Revokes a token (optional, for logout).
     */
    public void revokeToken(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
