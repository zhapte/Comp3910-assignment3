package com.corejsf;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ca.bcit.infosys.employee.Employee;
import ca.bcit.infosys.timesheet.Timesheet;

/**
 * REST resource providing CRUD-style operations for Timesheets.
 *
 * Base path: /api/timesheets
 *
 * Supported endpoints:
 *   • GET    /api/timesheets          → list timesheets (admin: all, user: own)
 *   • GET    /api/timesheets/{id}     → retrieve a specific timesheet
 *   • POST   /api/timesheets          → create the "current week" timesheet
 *   • PUT    /api/timesheets/{id}     → update a timesheet (rows + end date)
 *
 * Security model:
 *   - Authentication via Bearer tokens (AuthTokenStore)
 *   - Admins can view all timesheets
 *   - Non-admins can only access their own timesheets
 *   - Timesheets are editable until the *current week's Friday*
 */
@Path("/timesheets")
@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TimesheetResource {

    @Inject
    private AuthTokenStore tokenStore;

    @Inject
    private TimeSheetRepo timeSheetRepo;

    @Inject
    private CurrentUser currentUser;

    // ---- Helper methods -----------------------------------------------------

    /**
     * Validates Authorization header format and retrieves the Employee associated
     * with the provided token. If authentication fails, a 401 response is thrown.
     *
     * @param authHeader "Authorization: Bearer <token>"
     * @return the authenticated Employee
     */
    private Employee authenticate(String authHeader) {
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

        // Keep CurrentUser in sync for any code that still uses it
        currentUser.setEmployee(emp);
        return emp;
    }

    /**
     * Checks whether the given employee is an administrator.
     */
    private boolean isAdmin(Employee e) {
        return e instanceof Admin;
    }

    /**
     * Determines whether the requesting employee may view a given timesheet.
     * Admins have access to all timesheets; regular users only their own.
     */
    private boolean canAccessTimesheet(Employee requester, Timesheet ts) {
        if (ts == null) return false;
        if (isAdmin(requester)) return true;
        return ts.getEmployee() != null
                && ts.getEmployee().getEmpNumber() == requester.getEmpNumber();
    }

    /**
     * A timesheet is considered "editable" if its end date is >= this week's Friday.
     * Users cannot edit timesheets from previous weeks.
     */
    private boolean isEditable(Timesheet ts) {
        if (ts == null || ts.getEndDate() == null) return false;
        LocalDate thisFriday = LocalDate.now().with(DayOfWeek.FRIDAY);
        return !ts.getEndDate().isBefore(thisFriday);
    }

    /**
     * Helper method for converting a Timesheet entity into a TimesheetDto.
     */
    private TimesheetDto toDto(Timesheet ts) {
        Long id = timeSheetRepo.getIdFor(ts);
        return TimesheetDto.fromEntity(ts, id, isEditable(ts));
    }

    // ---- 4.3.1 GET /api/timesheets -----------------------------------------

    /**
     * Lists all accessible timesheets for the authenticated user.
     *
     * Admins → return every timesheet in the system  
     * Users  → return only their own timesheets
     */
    @GET
    public Response listTimesheets(@HeaderParam("Authorization") String authHeader) {
        Employee caller = authenticate(authHeader);

        List<Timesheet> sheets = isAdmin(caller)
                ? timeSheetRepo.getTimesheets()
                : timeSheetRepo.getTimesheets(caller);

        List<TimesheetDto> dtos = sheets.stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return Response.ok(dtos).build();
    }

    // ---- 4.3.2 GET /api/timesheets/{id} ------------------------------------

    /**
     * Retrieves a single timesheet by ID.
     * Enforces that:
     *   • Timesheet exists
     *   • User has permission to view it
     */
    @GET
    @Path("{id}")
    public Response getTimesheet(@HeaderParam("Authorization") String authHeader,
                                 @PathParam("id") Long id) {

        Employee caller = authenticate(authHeader);
        Timesheet ts = timeSheetRepo.loadById(id);

        if (ts == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorDto("Timesheet not found"))
                    .build();
        }

        if (!canAccessTimesheet(caller, ts)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorDto("You are not allowed to view this timesheet"))
                    .build();
        }

        return Response.ok(toDto(ts)).build();
    }

    // ---- 4.3.3 POST /api/timesheets ----------------------------------------

    /**
     * Creates a new "current week" timesheet for the authenticated user.
     *
     * The underlying TimeSheetRepo uses CurrentUser to assign ownership,
     * so we set the CurrentUser explicitly before creation.
     *
     * @return 201 Created with the newly created timesheet DTO
     */
    @POST
    public Response createTimesheet(@HeaderParam("Authorization") String authHeader,
                                    @Context UriInfo uriInfo) {

        Employee caller = authenticate(authHeader);

        // TimeSheetRepo.addTimesheet() uses CurrentUser internally
        currentUser.setEmployee(caller);
        String result = timeSheetRepo.addTimesheet();
        if ("no-user".equals(result)) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorDto("Could not create timesheet for current user"))
                    .build();
        }

        // Get the "current week" sheet for this user (the one we just created)
        Timesheet ts = timeSheetRepo.getCurrentTimesheet(caller);
        if (ts == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorDto("Failed to load newly created timesheet"))
                    .build();
        }

        Long id = timeSheetRepo.getIdFor(ts);
        TimesheetDto dto = TimesheetDto.fromEntity(ts, id, isEditable(ts));

        UriBuilder builder = uriInfo.getAbsolutePathBuilder().path(String.valueOf(id));
        return Response.created(builder.build()).entity(dto).build();  // 201 Created
    }

    // ---- 4.3.4 PUT /api/timesheets/{id} ------------------------------------

    /**
     * Updates an existing timesheet using data from a TimesheetDto.
     *
     * Validations:
     *  - Timesheet must exist
     *  - Caller must have permission to edit
     *  - Timesheet must still be editable (not from a past week)
     *  - Request body must not be null
     *
     * After applying the DTO to the entity, the modified timesheet is saved.
     */
    @PUT
    @Path("{id}")
    public Response updateTimesheet(@HeaderParam("Authorization") String authHeader,
                                    @PathParam("id") Long id,
                                    TimesheetDto body) {

        Employee caller = authenticate(authHeader);
        Timesheet ts = timeSheetRepo.loadById(id);

        if (ts == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorDto("Timesheet not found"))
                    .build();
        }

        if (!canAccessTimesheet(caller, ts)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorDto("You are not allowed to update this timesheet"))
                    .build();
        }

        if (!isEditable(ts)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(new ErrorDto("Timesheet is not editable (past week)"))
                    .build();
        }

        if (body == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorDto("Request body is required"))
                    .build();
        }

        // Apply incoming DTO to the entity
        body.applyToEntity(ts);

        // Persist changes; overload ensures we use the given id
        timeSheetRepo.save(ts, id);

        // You can choose 200 OK (with updated DTO) or 204 No Content.
        TimesheetDto updated = TimesheetDto.fromEntity(ts, id, isEditable(ts));
        return Response.ok(updated).build();
        // Or: return Response.noContent().build();
    }
}
