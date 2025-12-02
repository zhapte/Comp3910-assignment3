package com.corejsf;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import ca.bcit.infosys.timesheet.Timesheet;
import ca.bcit.infosys.timesheet.TimesheetRow;
import ca.bcit.infosys.employee.Employee;


/**
 * Data Transfer Object (DTO) representing a Timesheet.
 *
 * This class is a JSON-ready representation of a Timesheet entity used for REST
 * communication between the backend and frontend. It includes:
 *   - Timesheet metadata (id, employee info, end date)
 *   - A list of TimesheetRowDto objects
 *   - A flag indicating whether the timesheet is currently editable
 *
 * Conversion helpers:
 *   • {@link #fromEntity(Timesheet, Long, boolean)} — converts an entity to a DTO
 *   • {@link #applyToEntity(Timesheet)} — applies DTO state back into an entity
 */
public class TimesheetDto {

    private Long id;
    private int empNumber;
    private String employeeName;
    private String endDate;
    private boolean editable;
    private List<TimesheetRowDto> rows;

    
    /**
     * Build a TimesheetDto instance from a Timesheet entity.
     *
     * @param ts        the Timesheet entity
     * @param id        the unique id for this timesheet (from repo or constructed)
     * @param editable  whether the current user can modify this timesheet
     * @return a fully-populated TimesheetDto
     */
    public static TimesheetDto fromEntity(Timesheet ts, Long id, boolean editable) {
        TimesheetDto dto = new TimesheetDto();
        Employee e = ts.getEmployee();
        dto.id = id;
        dto.empNumber = (e != null) ? e.getEmpNumber() : -1;
        dto.employeeName = (e != null) ? e.getName() : "";
        dto.endDate = (ts.getEndDate() != null) ? ts.getEndDate().toString() : null;
        dto.editable = editable;

        dto.rows = ts.getDetails()
                     .stream()
                     .map(TimesheetRowDto::fromEntity)
                     .collect(Collectors.toList());

        return dto;
    }

    /**
     * Applies the data contained in this DTO back into an existing Timesheet entity.
     *
     * This method:
     *   • Updates the end date
     *   • Clears existing rows and rebuilds them from row DTOs
     *
     * Note:
     *   This method does NOT modify the Timesheet's owning Employee.
     *
     * @param ts the Timesheet entity to modify
     */
    public void applyToEntity(Timesheet ts) {
        if (endDate != null && !endDate.isBlank()) {
            ts.setEndDate(LocalDate.parse(endDate));
        }

        ts.getDetails().clear();
        if (rows != null) {
            for (TimesheetRowDto rowDto : rows) {
                TimesheetRow row = new TimesheetRow();
                rowDto.applyToEntity(row);
                ts.getDetails().add(row);
            }
        }
    }

    // getters / setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getEmpNumber() { return empNumber; }
    public void setEmpNumber(int empNumber) { this.empNumber = empNumber; }

    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    public List<TimesheetRowDto> getRows() { return rows; }
    public void setRows(List<TimesheetRowDto> rows) { this.rows = rows; }
}
