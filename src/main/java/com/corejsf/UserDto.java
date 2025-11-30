package com.corejsf;

import ca.bcit.infosys.employee.Employee;

public class UserDto {

    private String userName;
    private String name;
    private int empNumber;
    private boolean admin;

    public static UserDto fromEmployee(Employee e) {
        UserDto dto = new UserDto();
        dto.userName = e.getUserName();
        dto.name = e.getName();
        dto.empNumber = e.getEmpNumber();
        dto.admin = (e instanceof Admin);
        return dto;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getEmpNumber() { return empNumber; }
    public void setEmpNumber(int empNumber) { this.empNumber = empNumber; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
}
