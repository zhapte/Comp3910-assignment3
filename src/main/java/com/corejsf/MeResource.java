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
 * REST endpoint that returns information about the currently authenticated user.
 *
 * Base path: /api/currentuser
 *
 * This endpoint is useful for:
 *   • showing the logged-in user's name / role in the UI
 *   • verifying token validity
 *   • retrieving the user's employee number and admin status
 *
 * Requires:
 *     Authorization: Bearer <token>
 */
@Path("/currentuser")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class MeResource {

    @Inject
    private AuthTokenStore tokenStore;

    @Inject
    private CurrentUser currentUser;

    /**
     * GET /api/currentuser
     *
     * Returns a UserDto describing the user associated with the given token.
     *
     * Behavior:
     *   • Validates that a Bearer token is present
     *   • Resolves the Employee mapped to that token
     *   • Updates CurrentUser for compatibility with legacy BCIT-provided code
     *   • Returns a UserDto with basic user profile information
     *
     * Error responses:
     *   401 Unauthorized → missing token or invalid/expired token
     *
     * @param authHeader Authorization header ("Bearer <token>")
     * @return 200 OK with UserDto, or 401 Unauthorized with ErrorDto
     */
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
