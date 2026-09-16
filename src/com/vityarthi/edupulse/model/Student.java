package com.vityarthi.edupulse.model;

import com.vityarthi.edupulse.common.Role;
import java.time.LocalDateTime;

/**
 * Concrete Student entity extending User.
 * Demonstrates Inheritance and specialized domain attributes.
 */
public class Student extends User {
    private String regNo;
    private String department;
    private int semester;
    private double cgpa;
    private int earnedCredits;
    private double scholarshipPercentage;

    public Student(String id, String name, String email, String passwordHash, String salt,
                   String regNo, String department, int semester) {
        super(id, name, email, passwordHash, salt, Role.STUDENT);
        this.regNo = regNo;
        this.department = department;
        this.semester = semester;
        this.cgpa = 0.0;
        this.earnedCredits = 0;
        this.scholarshipPercentage = 0.0;
    }

    public Student(String id, String name, String email, String passwordHash, String salt,
                   LocalDateTime createdAt, boolean active, String regNo, String department,
                   int semester, double cgpa, int earnedCredits, double scholarshipPercentage) {
        super(id, name, email, passwordHash, salt, Role.STUDENT, createdAt, active);
        this.regNo = regNo;
        this.department = department;
        this.semester = semester;
        this.cgpa = cgpa;
        this.earnedCredits = earnedCredits;
        this.scholarshipPercentage = scholarshipPercentage;
    }

    @Override
    public String getProfileSummary() {
        return String.format("Student: %s | Reg No: %s | Dept: %s (Sem %d) | CGPA: %.2f | Credits: %d",
                name, regNo, department, semester, cgpa, earnedCredits);
    }

    public String getRegNo() { return regNo; }
    public void setRegNo(String regNo) { this.regNo = regNo; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = Math.round(cgpa * 100.0) / 100.0; }
    public int getEarnedCredits() { return earnedCredits; }
    public void setEarnedCredits(int earnedCredits) { this.earnedCredits = earnedCredits; }
    public double getScholarshipPercentage() { return scholarshipPercentage; }
    public void setScholarshipPercentage(double scholarshipPercentage) { this.scholarshipPercentage = scholarshipPercentage; }
}
