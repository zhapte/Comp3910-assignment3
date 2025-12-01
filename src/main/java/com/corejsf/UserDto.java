package com.corejsf;

import ca.bcit.infosys.employee.Employee;

public class UserDto {

    private String userName;
    private String name;
    private int empNumber;
    private boolean admin;
    private String role;   // NEW FIELD

    /**
     * Builds a UserDto from an Employee.
     */
    public static UserDto fromEmployee(Employee e) {
        UserDto dto = new UserDto();

        dto.userName = e.getUserName();
        dto.name = e.getName();
        dto.empNumber = e.getEmpNumber();

        // Determine role
        dto.admin = (e instanceof Admin);
        dto.role = dto.admin ? "ADMIN" : "USER";

        return dto;
    }

    // ----- Getters & Setters -----

    public String getUserName() { 
        return userName; 
    }
    public void setUserName(String userName) { 
        this.userName = userName; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public int getEmpNumber() { 
        return empNumber; 
    }
    public void setEmpNumber(int empNumber) { 
        this.empNumber = empNumber; 
    }

    public boolean isAdmin() { 
        return admin; 
    }
    public void setAdmin(boolean admin) { 
        this.admin = admin; 
        this.role = admin ? "ADMIN" : "USER";  // keep consistent
    }

    public String getRole() { 
        return role; 
    }
    public void setRole(String role) { 
        this.role = role; 
        this.admin = "ADMIN".equalsIgnoreCase(role); // keep consistent
    }
}
