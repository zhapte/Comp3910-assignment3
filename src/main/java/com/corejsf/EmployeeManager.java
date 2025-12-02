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
     * Validates the Authorization header, resolves the employee from the token,
     * or throws a 401 if invalid / missing.
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
     * Validates that the caller is an authenticated admin user.
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
     * Utility: find by empNumber by scanning the list from EmployeeRepo.
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

    @Override
    public List<Employee> getAll(String authHeader) {
        requireAdmin(authHeader);
        return employeeRepo.getEmployees();
    }

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
	
		// Decide Admin vs User based on dto.isAdmin()
		Employee emp = dto.isAdmin() ? new Admin() : new User();
		emp.setName(dto.getName());
		emp.setEmpNumber(dto.getEmpNumber());     // 0 = let repo assign
		emp.setUserName(dto.getUserName().trim());
	
		// Persist via existing repo (enforces unique username/empNumber, sets role)
		employeeRepo.addEmployee(emp);
		Employee saved = employeeRepo.getEmployee(emp.getUserName());
		if (saved == null) {
			saved = emp;
		}
	
		return UserDto.fromEmployee(saved);
	
	}


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
