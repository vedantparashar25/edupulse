package com.vityarthi.edupulse.model;

import com.vityarthi.edupulse.common.Role;
import java.time.LocalDateTime;

/**
 * System Administrator entity extending User.
 */
public class Admin extends User {
    private String adminDepartment;
    private boolean superAdmin;

    public Admin(String id, String name, String email, String passwordHash, String salt,
                 String adminDepartment, boolean superAdmin) {
        super(id, name, email, passwordHash, salt, Role.ADMIN);
        this.adminDepartment = adminDepartment;
        this.superAdmin = superAdmin;
    }

    public Admin(String id, String name, String email, String passwordHash, String salt,
                 LocalDateTime createdAt, boolean active, String adminDepartment, boolean superAdmin) {
        super(id, name, email, passwordHash, salt, Role.ADMIN, createdAt, active);
        this.adminDepartment = adminDepartment;
        this.superAdmin = superAdmin;
    }

    @Override
    public String getProfileSummary() {
        return String.format("Admin: %s | Unit: %s | SuperAdmin: %b", name, adminDepartment, superAdmin);
    }

    public String getAdminDepartment() { return adminDepartment; }
    public boolean isSuperAdmin() { return superAdmin; }
}
