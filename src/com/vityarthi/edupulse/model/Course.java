package com.vityarthi.edupulse.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Course offering entity with credit weight, prerequisites, and seat limits.
 */
public class Course implements Serializable {
    private static final long serialVersionUID = 1L;

    private String code;
    private String title;
    private int credits;
    private String department;
    private String facultyId;
    private int capacity;
    private int enrolledCount;
    private List<String> prerequisites;
    private String description;

    public Course(String code, String title, int credits, String department, String facultyId, int capacity, String description) {
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.department = department;
        this.facultyId = facultyId;
        this.capacity = capacity;
        this.enrolledCount = 0;
        this.prerequisites = new ArrayList<>();
        this.description = description;
    }

    public Course(String code, String title, int credits, String department, String facultyId,
                  int capacity, int enrolledCount, List<String> prerequisites, String description) {
        this.code = code;
        this.title = title;
        this.credits = credits;
        this.department = department;
        this.facultyId = facultyId;
        this.capacity = capacity;
        this.enrolledCount = enrolledCount;
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
        this.description = description;
    }

    public boolean hasAvailableSeats() {
        return enrolledCount < capacity;
    }

    public void incrementEnrolled() {
        this.enrolledCount++;
    }

    public void decrementEnrolled() {
        if (this.enrolledCount > 0) this.enrolledCount--;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public int getCredits() { return credits; }
    public String getDepartment() { return department; }
    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }
    public int getCapacity() { return capacity; }
    public int getEnrolledCount() { return enrolledCount; }
    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return String.format("%s: %s (%d Credits) [%d/%d Enrolled]",
                code, title, credits, enrolledCount, capacity);
    }
}
