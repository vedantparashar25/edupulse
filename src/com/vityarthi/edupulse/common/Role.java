package com.vityarthi.edupulse.common;

/**
 * Role-Based Access Control (RBAC) definitions.
 */
public enum Role {
    ADMIN("System Administrator", 1),
    FACULTY("Academic Faculty", 2),
    STUDENT("Registered Student", 3);

    private final String displayName;
    private final int accessLevel;

    Role(String displayName, int accessLevel) {
        this.displayName = displayName;
        this.accessLevel = accessLevel;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public boolean canManageCourses() {
        return this == ADMIN || this == FACULTY;
    }

    public boolean canManageUsers() {
        return this == ADMIN;
    }

    public boolean canGradeStudents() {
        return this == FACULTY || this == ADMIN;
    }
}
