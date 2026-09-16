package com.vityarthi.edupulse.model;

import com.vityarthi.edupulse.common.Role;
import java.time.LocalDateTime;

/**
 * Concrete Faculty entity extending User.
 */
public class Faculty extends User {
    private String employeeId;
    private String department;
    private String designation;
    private String cabinRoom;

    public Faculty(String id, String name, String email, String passwordHash, String salt,
                   String employeeId, String department, String designation, String cabinRoom) {
        super(id, name, email, passwordHash, salt, Role.FACULTY);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.cabinRoom = cabinRoom;
    }

    public Faculty(String id, String name, String email, String passwordHash, String salt,
                   LocalDateTime createdAt, boolean active, String employeeId, String department,
                   String designation, String cabinRoom) {
        super(id, name, email, passwordHash, salt, Role.FACULTY, createdAt, active);
        this.employeeId = employeeId;
        this.department = department;
        this.designation = designation;
        this.cabinRoom = cabinRoom;
    }

    @Override
    public String getProfileSummary() {
        return String.format("Faculty: %s (%s) | Dept: %s | Cabin: %s",
                name, designation, department, cabinRoom);
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getCabinRoom() { return cabinRoom; }
    public void setCabinRoom(String cabinRoom) { this.cabinRoom = cabinRoom; }
}
