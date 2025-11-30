package com.corejsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.HeaderParam;

import ca.bcit.infosys.employee.Credentials;
import ca.bcit.infosys.employee.Employee;


@Path("/")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    private AuthService authService;

    @Inject
    private AuthTokenStore tokenStore;

    @POST
	@Path("/login")
    public Response login(LoginRequest request) {
        if (request == null
                || request.getUserName() == null
                || request.getPassword() == null) {
            // Missing fields -> 400 Bad Request
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity(new ErrorDto("userName and password are required"))
                           .build();
        }

        // Trim username like your LoginBean does
        String userName = request.getUserName().trim();

        Credentials cred = new Credentials();
        cred.setUserName(userName);
        cred.setPassword(request.getPassword());

        // Reuse your existing authentication logic
        Employee emp = authService.authenticate(cred);
        if (emp == null) {
            // Invalid credentials -> 401 Unauthorized
            return Response.status(Response.Status.UNAUTHORIZED)
                           .entity(new ErrorDto("Invalid username or password"))
                           .build();
        }

        // Generate token and store mapping
        String token = tokenStore.issueToken(emp);

        // Build response DTO
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setUserName(emp.getUserName());
        resp.setName(emp.getName());
        resp.setEmpNumber(emp.getEmpNumber());
        resp.setAdmin(authService.isAdmin(emp));

        return Response.ok(resp).build();
    }
	
	@POST
	@Path("/logout")
	@Produces(MediaType.APPLICATION_JSON)
	public Response logout(@HeaderParam("Authorization") String authHeader) {
	
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return Response.status(Response.Status.UNAUTHORIZED)
					.entity(new ErrorDto("Missing Authorization header"))
					.build();
		}
	
		String token = authHeader.substring("Bearer ".length()).trim();
	
		// Remove token
		tokenStore.revokeToken(token);
	
		return Response.ok(new MessageDto("Logged out successfully")).build();
	}
}
