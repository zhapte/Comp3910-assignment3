package com.corejsf;

import ca.bcit.infosys.employee.Employee;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import com.corejsf.UserDto;

import java.util.List;

/**
 * Server-side implementation of EmployeeService.
 *
 * This bean exposes employee-related operations such as:
 * - Searching employees
 * - Creating (persisting) new users
 * - Deleting employees
 * - Changing / resetting passwords
 *
 * Security:
 *   Most operations require admin privileges, enforced using helper
 *   methods requireUser() and requireAdmin().
 */
@Dependent
@Stateless
public class EmployeeManager implements EmployeeService {

    @Inject
    private EmployeeRepo employeeRepo;

    @Inject
    private AuthTokenStore tokenStore;

    @Inject
    private AuthService authService;

    // Helper methods 

    /**
     * Validates the Authorization header and resolves an Employee from it.
     *
     * Expected header format:
     *     Authorization: Bearer <token>
     *
     * @param authHeader the Authorization header supplied by the client
     * @return the authenticated Employee
     * @throws WebApplicationException (401) if missing, malformed, or invalid
     */
    private Employee requireUser(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorDto("Missing or invalid Authorization header"))
                            .build());
        }

        String token = authHeader.substring("Bearer ".length()).trim();
        Employee emp = tokenStore.getEmployeeForToken(token);
        if (emp == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ErrorDto("Invalid or expired token"))
                            .build());
        }
        return emp;
    }

    /**
     * Validates that the caller is an authenticated admin.
     *
     * @param authHeader the Authorization header
     * @return the Employee representing the admin caller
     * @throws WebApplicationException (403) if caller is not an admin
     */
    private Employee requireAdmin(String authHeader) {
        Employee caller = requireUser(authHeader);
        if (!authService.isAdmin(caller)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.FORBIDDEN)
                            .entity(new ErrorDto("Admin privileges required"))
                            .build());
        }
        return caller;
    }

    /**
     * Helper: find an employee by empNumber by scanning the entire repo list.
     *
     * (Repo doesn't expose this directly, so we do a linear search.)
     *
     * @param empNumber employee number
     * @return matching Employee or null if not found
     */
    private Employee findByEmpNumberInternal(int empNumber) {
        for (Employee e : employeeRepo.getEmployees()) {
            if (e.getEmpNumber() == empNumber) {
                return e;
            }
        }
        return null;
    }

    // EmployeeService API 

    /**
     * Find an employee by numeric employee number.
     * Admin-only.
     */
    @Override
    public Employee findByEmpNumber(String authHeader, int empNumber) {
        requireAdmin(authHeader);

        Employee e = findByEmpNumberInternal(empNumber);
        if (e == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorDto("Employee not found: " + empNumber))
                            .build());
        }
        return e;
    }

    
    /**
     * Find an employee by username.
     * Admin-only.
     */
    @Override
    public Employee findByUserName(String authHeader, String userName) {
        requireAdmin(authHeader);

        Employee e = employeeRepo.getEmployee(userName);
        if (e == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorDto("Employee not found: " + userName))
                            .build());
        }
        return e;
    }

    /**
     * Retrieve all employees.
     * Admin-only.
     */
    @Override
    public List<Employee> getAll(String authHeader) {
        requireAdmin(authHeader);
        return employeeRepo.getEmployees();
    }

    /**
     * Create and persist a new Employee (User or Admin).
     *
     * Validates:
     *  - dto is not null
     *  - userName is required
     *  - name is required
     *
     * Uses dto.isAdmin() to decide whether to instantiate an Admin or User.
     *
     * @return a UserDto representation of the saved employee
     */
    @Override
	public UserDto persist(String authHeader, UserDto dto) {
		// Admin-only
		requireAdmin(authHeader);
	
		if (dto == null) {
			throw new WebApplicationException(
				Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorDto("User payload must not be null"))
						.build());
		}
		if (dto.getUserName() == null || dto.getUserName().isBlank()) {
			throw new WebApplicationException(
				Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorDto("userName is required"))
						.build());
		}
		if (dto.getName() == null || dto.getName().isBlank()) {
			throw new WebApplicationException(
				Response.status(Response.Status.BAD_REQUEST)
						.entity(new ErrorDto("name is required"))
						.build());
		}
		
		Employee emp = dto.isAdmin() ? new Admin() : new User();
		emp.setName(dto.getName().trim());
		emp.setUserName(dto.getUserName().trim());
		emp.setEmpNumber(dto.getEmpNumber());
		
		employeeRepo.addEmployee(emp);
		
		Employee saved = employeeRepo.getEmployee(emp.getUserName());
		
		if (saved == null) {
			saved = emp;
		}
		
		return UserDto.fromEmployee(saved);
	}


    /**
     * Remove an employee by empNumber.
     * Admin-only.
     *
     * Prevents an admin from deleting themselves.
     */
    @Override
    public void remove(String authHeader, int empNumber) {
        Employee caller = requireAdmin(authHeader);

        Employee toDelete = findByEmpNumberInternal(empNumber);
        if (toDelete == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(new ErrorDto("Employee not found: " + empNumber))
                            .build());
        }

       
        if (caller.getEmpNumber() == empNumber) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .entity(new ErrorDto("Admins cannot delete themselves"))
                            .build());
        }

        employeeRepo.deleteEmployee(toDelete);
    }
	
    /**
     * Reset the password for a given user.
     * Admin-only.
     *
     * The password is reset to the default literal: "password".
     */
	@Override
	public void resetPassword(String authHeader, String userName) {
		requireAdmin(authHeader);
	
		if (userName == null || userName.isBlank()) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorDto("userName must not be blank"))
							.build());
		}
	
		Employee e = employeeRepo.getEmployee(userName);
		if (e == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.NOT_FOUND)
							.entity(new ErrorDto("User not found: " + userName))
							.build());
		}
	
		employeeRepo.changePassword(userName, "password");
	}

	/**
     * Allows a logged-in user to change **their own** password.
     *
     * Restrictions:
     *  - Only the employee themselves can change their password
     *  - Request body must contain a "password" field
     */
	@Override
	public void changePassword(String authHeader,
							String userName,
							Map<String, String> body) {
		Employee caller = requireUser(authHeader);
	
		// only allow self-change (or add admin override if you want)
		if (!caller.getUserName().equalsIgnoreCase(userName)) {
			throw new WebApplicationException(
					Response.status(Response.Status.FORBIDDEN)
							.entity(new ErrorDto("You can only change your own password"))
							.build());
		}
	
		if (body == null || !body.containsKey("password")) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorDto("Missing 'password' in request body"))
							.build());
		}
	
		String newPassword = body.get("password");
		if (newPassword == null || newPassword.isBlank()) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(new ErrorDto("Password must not be blank"))
							.build());
		}
	
		employeeRepo.changePassword(userName, newPassword);
	}


}
