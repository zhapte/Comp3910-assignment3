package com.corejsf;

import ca.bcit.infosys.employee.Employee;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/employees")
public interface EmployeeService {
    
    /**
     * Return all employees in the system
     * @return an array of employees as JSON
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Employee[] getAll();
    
    /** Get employee by employee number */
    @GET
    @Path("/{empNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    Employee findByEmpNumber(@PathParam("empNumber") int empNumber);
    
    /** Get employee by user name */
    @GET
    @Path("/username/{userName}")
    @Produces(MediaType.APPLICATION_JSON)
    Employee findByUserName(@PathParam("userName") String userName);
    
    /**
     * Create a new employee
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    void persist(Employee employee);
    
    /**
     * Update an existing employee
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    void merge(Employee employee);
    
    
    /** Delete an employee by employee number */
    @DELETE
    @Path("/{empNumber}")
    void remove(@PathParam("empNumber") int empNumber);
    
    
    
    
    
    

}
