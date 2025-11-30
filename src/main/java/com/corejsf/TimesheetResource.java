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
 * REST resource for timesheet operations.
 *
 * Base path: /api/timesheets
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

    private boolean isAdmin(Employee e) {
        return e instanceof Admin;
    }

    private boolean canAccessTimesheet(Employee requester, Timesheet ts) {
        if (ts == null) return false;
        if (isAdmin(requester)) return true;
        return ts.getEmployee() != null
                && ts.getEmployee().getEmpNumber() == requester.getEmpNumber();
    }

    private boolean isEditable(Timesheet ts) {
        if (ts == null || ts.getEndDate() == null) return false;
        LocalDate thisFriday = LocalDate.now().with(DayOfWeek.FRIDAY);
        return !ts.getEndDate().isBefore(thisFriday);
    }

    private TimesheetDto toDto(Timesheet ts) {
        Long id = timeSheetRepo.getIdFor(ts);
        return TimesheetDto.fromEntity(ts, id, isEditable(ts));
    }

    // ---- 4.3.1 GET /api/timesheets -----------------------------------------

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
