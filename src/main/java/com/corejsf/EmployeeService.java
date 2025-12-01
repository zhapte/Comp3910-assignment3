package com.corejsf;

import ca.bcit.infosys.employee.Employee;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import com.corejsf.UserDto;


import java.util.List;

/**
 * REST API for managing employees.
 *
 */
@Path("/employees")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface EmployeeService {

    /**
     * Finds an employee by empNumber.
     *
     */
    @GET
    @Path("{empNumber}")
    Employee findByEmpNumber(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("empNumber") int empNumber);

    /**
     * Finds an employee by username (case-insensitive).
     *
     */
    @GET
    @Path("by-username/{userName}")
    Employee findByUserName(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("userName") String userName);

    /**
     * Returns all employees in the system.
     *
     */
    @GET
    List<Employee> getAll(
            @HeaderParam("Authorization") String authHeader);

    /**
     * Creates a new employee.
     */
    @POST
    void persist(
            @HeaderParam("Authorization") String authHeader,
            UserDto userDto);


    /**
     * Deletes an employee by empNumber.
     *
     */
    @DELETE
    @Path("{empNumber}")
    void remove(
            @HeaderParam("Authorization") String authHeader,
            @PathParam("empNumber") int empNumber);
			
	
	/**
	* Reset password to default ("password") – admin only.
	*/
	@POST
	@Path("{userName}/reset-password")
	void resetPassword(
			@HeaderParam("Authorization") String authHeader,
			@PathParam("userName") String userName);
	
	/**
	* Change password of the specified user.
	*
	* Server enforces: only the logged-in user can change their own password.
	*/
	@PUT
	@Path("{userName}/password")
	void changePassword(
			@HeaderParam("Authorization") String authHeader,
			@PathParam("userName") String userName,
			Map<String, String> body);
}
