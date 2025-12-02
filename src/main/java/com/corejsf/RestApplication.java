package com.corejsf;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;


/**
 * JAX-RS application configuration class.
 *
 * This class activates JAX-RS and sets the base URI path for all REST
 * endpoints in this application. Every resource annotated with @Path
 * will be accessible under:
 *
 *     /api/... 
 *
 * For example:
 *   - AuthResource's /login → /api/login
 *   - EmployeeResource's /users → /api/users
 *
 * Extending {@link Application} is the standard way to bootstrap
 * RESTful services in Jakarta EE.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

}
