package com.vityarthi.edupulse.pattern.factory;

import com.vityarthi.edupulse.common.Role;
import com.vityarthi.edupulse.model.Admin;
import com.vityarthi.edupulse.model.Faculty;
import com.vityarthi.edupulse.model.Student;
import com.vityarthi.edupulse.model.User;
import com.vityarthi.edupulse.util.SecurityUtil;
import java.util.Map;

/**
 * Factory Design Pattern for instantiating polymorphic User objects.
 */
public class UserFactory {

    public static User createUser(Role role, String id, String name, String email,
                                  String plainPassword, Map<String, String> attributes) {
        String salt = SecurityUtil.generateSalt();
        String passwordHash = SecurityUtil.hashPassword(plainPassword, salt);

        switch (role) {
            case STUDENT:
                String regNo = attributes.getOrDefault("regNo", "REG-" + id);
                String dept = attributes.getOrDefault("department", "Computer Science");
                int sem = Integer.parseInt(attributes.getOrDefault("semester", "1"));
                return new Student(id, name, email, passwordHash, salt, regNo, dept, sem);

            case FACULTY:
                String empId = attributes.getOrDefault("employeeId", "FAC-" + id);
                String fDept = attributes.getOrDefault("department", "Computer Science");
                String designation = attributes.getOrDefault("designation", "Assistant Professor");
                String cabin = attributes.getOrDefault("cabinRoom", "AB1-302");
                return new Faculty(id, name, email, passwordHash, salt, empId, fDept, designation, cabin);

            case ADMIN:
                String admDept = attributes.getOrDefault("adminDepartment", "Registrar Office");
                boolean superAdmin = Boolean.parseBoolean(attributes.getOrDefault("superAdmin", "false"));
                return new Admin(id, name, email, passwordHash, salt, admDept, superAdmin);

            default:
                throw new IllegalArgumentException("Unknown Role: " + role);
        }
    }
}
