package com.corejsf;

import ca.bcit.infosys.employee.Employee;
import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * JSF-side REST client for the Employee REST API.
 *
 */
@Dependent
public class EmployeeClient implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String baseURI;
    static {
      
        String host = System.getenv("TIMESHEETS_SERVICE_HOST");
        String port = System.getenv("TIMESHEETS_SERVICE_PORT");

        if (host == null || host.isBlank()) {
            host = "localhost";
        }
        if (port == null || port.isBlank()) {
            port = "8080";
        }

        baseURI = "http://" + host + ":" + port + "/api";
    }

    /** The current user's Bearer token (set after login). */
    private String authToken;

    /**
     * Sets the Bearer token used for subsequent REST calls.
     *
     * @param authToken token string returned by /api/login
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    /** Returns the current token (optional, for debugging/UI). */
    public String getAuthToken() {
        return authToken;
    }

    // Helper to build authorized requests 

    private Invocation.Builder authorizedRequest(String path) {
        if (authToken == null || authToken.isBlank()) {
            throw new IllegalStateException("No auth token set on EmployeeClient");
        }
        Client client = ClientBuilder.newClient();
        return client
                .target(baseURI)
                .path(path)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken);
    }

    // REST methods

    public Employee findByEmpNumber(int empNumber) {
        Invocation.Builder req = authorizedRequest("employees/" + empNumber);
        try (Response res = req.get()) {
            if (res.getStatus() == 200) {
                return res.readEntity(Employee.class);
            }
            // you can add more detailed error handling/logging here
            throw new RuntimeException("findByEmpNumber failed: HTTP " + res.getStatus());
        }
    }

    public Employee findByUserName(String userName) {
        Invocation.Builder req = authorizedRequest("employees/by-username/" + userName);
        try (Response res = req.get()) {
            if (res.getStatus() == 200) {
                return res.readEntity(Employee.class);
            }
            throw new RuntimeException("findByUserName failed: HTTP " + res.getStatus());
        }
    }

    public List<Employee> getAll() {
        Invocation.Builder req = authorizedRequest("employees");
        try (Response res = req.get()) {
            if (res.getStatus() == 200) {
                Employee[] array = res.readEntity(Employee[].class);
                return Arrays.asList(array);
            }
            throw new RuntimeException("getAll failed: HTTP " + res.getStatus());
        }
    }

    public void persist(Employee employee) {
        Invocation.Builder req = authorizedRequest("employees");
        try (Response res = req.post(Entity.entity(employee, MediaType.APPLICATION_JSON))) {
            if (res.getStatus() >= 200 && res.getStatus() < 300) {
                return;
            }
            throw new RuntimeException("persist failed: HTTP " + res.getStatus());
        }
    }

    public void merge(Employee employee) {
        Invocation.Builder req = authorizedRequest("employees");
        try (Response res = req.put(Entity.entity(employee, MediaType.APPLICATION_JSON))) {
            if (res.getStatus() >= 200 && res.getStatus() < 300) {
                return;
            }
            throw new RuntimeException("merge failed: HTTP " + res.getStatus());
        }
    }

    public void remove(int empNumber) {
        Invocation.Builder req = authorizedRequest("employees/" + empNumber);
        try (Response res = req.delete()) {
            if (res.getStatus() >= 200 && res.getStatus() < 300) {
                return;
            }
            throw new RuntimeException("remove failed: HTTP " + res.getStatus());
        }
    }
}