package com.corejsf;

import ca.bcit.infosys.timesheet.TimesheetRow;

public class TimesheetRowDto {

    private int projectId;
    private String workPackageId;
    private float[] hours;   // length 7, Sat..Fri
    private String notes;

    public static TimesheetRowDto fromEntity(TimesheetRow row) {
        TimesheetRowDto dto = new TimesheetRowDto();
        dto.projectId = row.getProjectId();
        dto.workPackageId = row.getWorkPackageId();
        dto.hours = row.getHours(); // model already uses Sat..Fri array
        dto.notes = row.getNotes();
        return dto;
    }

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
