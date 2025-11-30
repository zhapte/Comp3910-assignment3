package com.corejsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ca.bcit.infosys.employee.Employee;

/**
 * Returns information about the currently authenticated user.
 */
@Path("/currentuser")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    @Inject
    private AuthTokenStore tokenStore;

    @Inject
    private CurrentUser currentUser;

    @GET
    public Response getCurrentUser(@HeaderParam("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorDto("Missing or invalid Authorization header"))
                    .build();
        }

        String token = authHeader.substring("Bearer ".length()).trim();

        Employee emp = tokenStore.getEmployeeForToken(token);
        if (emp == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorDto("Invalid or expired token"))
                    .build();
        }

        currentUser.setEmployee(emp);

        UserDto dto = UserDto.fromEmployee(emp);

        return Response.ok(dto).build();
    }
}
