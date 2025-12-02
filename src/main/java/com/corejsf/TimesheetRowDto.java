package com.corejsf;

import ca.bcit.infosys.timesheet.TimesheetRow;


/**
 * Data Transfer Object (DTO) representing a single row within a Timesheet.
 *
 * A TimesheetRow includes:
 *   • projectId        – numeric project identifier
 *   • workPackageId    – work package code (string)
 *   • hours            – float[7] array representing hours for Sat..Fri
 *   • notes            – optional comments for the row
 *
 * This DTO is used for all REST API operations involving timesheet rows.
 * It provides conversion helpers to map between the entity model and REST JSON.
 */
public class TimesheetRowDto {

    private int projectId;
    private String workPackageId;
    private float[] hours;   // length 7, Sat..Fri
    private String notes;

    
    /**
     * Converts a TimesheetRow entity into a DTO.
     *
     * @param row the entity row to convert
     * @return a populated TimesheetRowDto
     */
    public static TimesheetRowDto fromEntity(TimesheetRow row) {
        TimesheetRowDto dto = new TimesheetRowDto();
        dto.projectId = row.getProjectId();
        dto.workPackageId = row.getWorkPackageId();
        dto.hours = row.getHours(); // model already uses Sat..Fri array
        dto.notes = row.getNotes();
        return dto;
    }

    /**
     * Applies this DTO's state back into a TimesheetRow entity.
     *
     * Responsibilities:
     *   - overwrite projectId and workPackageId
     *   - validate hours array (must be length 7)
     *   - update notes field
     *
     * If hours[] is missing or incorrectly sized, a default 0-filled array is used.
     *
     * @param row the entity row to modify
     */
    public void applyToEntity(TimesheetRow row) {
        row.setProjectId(projectId);
        row.setWorkPackageId(workPackageId != null ? workPackageId : "");
        if (hours != null && hours.length == 7) {
            row.setHours(hours);
        } else {
            row.setHours(new float[]{0,0,0,0,0,0,0});
        }
        row.setNotes(notes);
    }

    // getters / setters

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public String getWorkPackageId() { return workPackageId; }
    public void setWorkPackageId(String workPackageId) { this.workPackageId = workPackageId; }

    public float[] getHours() { return hours; }
    public void setHours(float[] hours) { this.hours = hours; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
