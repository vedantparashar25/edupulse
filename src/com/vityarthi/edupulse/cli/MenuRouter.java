package com.vityarthi.edupulse.cli;

import com.vityarthi.edupulse.common.AppException;
import com.vityarthi.edupulse.common.Role;
import com.vityarthi.edupulse.model.*;
import com.vityarthi.edupulse.pattern.strategy.AbsoluteGradingStrategy;
import com.vityarthi.edupulse.pattern.strategy.RelativeGradingStrategy;
import com.vityarthi.edupulse.pattern.strategy.ScholarshipStrategy;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.service.*;
import com.vityarthi.edupulse.util.AnsiUtil;
import com.vityarthi.edupulse.util.AsyncAuditLogger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles role-based interactive user menus and navigation.
 */
public class MenuRouter {
    private final AuthService authService;
    private final CourseService courseService;
    private final AttendanceService attendanceService;
    private final AnalyticsService analyticsService;
    private final FeeService feeService;
    private final Repository<User, String> userRepo;
    private final ConsoleView view;

    public MenuRouter(AuthService authService,
                      CourseService courseService,
                      AttendanceService attendanceService,
                      AnalyticsService analyticsService,
                      FeeService feeService,
                      Repository<User, String> userRepo,
                      ConsoleView view) {
        this.authService = authService;
        this.courseService = courseService;
        this.attendanceService = attendanceService;
        this.analyticsService = analyticsService;
        this.feeService = feeService;
        this.userRepo = userRepo;
        this.view = view;
    }

    public void routeUser(User user) {
        switch (user.getRole()) {
            case ADMIN -> runAdminMenu((Admin) user);
            case FACULTY -> runFacultyMenu((Faculty) user);
            case STUDENT -> runStudentMenu((Student) user);
        }
    }

    private void runAdminMenu(Admin admin) {
        while (true) {
            AnsiUtil.printHeader("ADMIN DASHBOARD - " + admin.getName());
            System.out.println("1. View All Registered Users");
            System.out.println("2. Register New User (Student / Faculty / Admin)");
            System.out.println("3. Create New Course Offering");
            System.out.println("4. View All Courses & Enrolled Stats");
            System.out.println("5. View Student Merit Leaderboard (CGPA Streams)");
            System.out.println("6. Run At-Risk Attendance Audit (< 75%)");
            System.out.println("7. View Recent Security & System Audit Logs");
            System.out.println("8. Logout Session");

            int choice = view.readInt("Select option", 1, 8);
            switch (choice) {
                case 1 -> view.displayUsers(userRepo.findAll());
                case 2 -> handleUserRegistration();
                case 3 -> handleCreateCourse();
                case 4 -> view.displayCourses(courseService.getAllCourses());
                case 5 -> view.displayLeaderboard(analyticsService.getTopPerformers(10));
                case 6 -> handleAttendanceAudit();
                case 7 -> handleViewAuditLogs();
                case 8 -> {
                    authService.logout();
                    AnsiUtil.printInfo("Admin logged out successfully.");
                    return;
                }
            }
        }
    }

    private void runFacultyMenu(Faculty faculty) {
        while (true) {
            AnsiUtil.printHeader("FACULTY PORTAL - " + faculty.getName() + " (" + faculty.getDepartment() + ")");
            System.out.println("1. View My Assigned Courses");
            System.out.println("2. View Students Enrolled in a Course");
            System.out.println("3. Mark Daily Session Attendance (Trigger Debarment Observer)");
            System.out.println("4. Evaluate & Grade Course (Absolute / Relative Strategy)");
            System.out.println("5. View Course Performance & Grade Distribution");
            System.out.println("6. Logout Session");

            int choice = view.readInt("Select option", 1, 6);
            switch (choice) {
                case 1 -> view.displayCourses(courseService.getAllCourses().stream()
                        .filter(c -> c.getFacultyId().equals(faculty.getId())).toList());
                case 2 -> handleFacultyViewStudents();
                case 3 -> handleMarkAttendance(faculty);
                case 4 -> handleGradeCourse();
                case 5 -> handleCourseAnalytics();
                case 6 -> {
                    authService.logout();
                    AnsiUtil.printInfo("Faculty logged out successfully.");
                    return;
                }
            }
        }
    }

    private void runStudentMenu(Student student) {
        while (true) {
            AnsiUtil.printHeader("STUDENT DESK - " + student.getName() + " (" + student.getRegNo() + ")");
            System.out.println("1. View Profile & Academic Summary");
            System.out.println("2. Browse Course Catalog & Enroll");
            System.out.println("3. Drop Enrolled Course");
            System.out.println("4. View My Enrolled Courses, Grades & CGPA");
            System.out.println("5. Check My Subject-wise Attendance & Status");
            System.out.println("6. Generate Tuition Fee Invoice (Scholarship Strategy)");
            System.out.println("7. Logout Session");

            int choice = view.readInt("Select option", 1, 7);
            switch (choice) {
                case 1 -> {
                    AnsiUtil.printHeader("MY ACADEMIC PROFILE");
                    System.out.println(student.getProfileSummary());
                    System.out.println("Registered Email: " + student.getEmail());
                }
                case 2 -> handleStudentEnroll(student);
                case 3 -> handleStudentDrop(student);
                case 4 -> {
                    analyticsService.computeAndUpdateCGPA(student.getId());
                    view.displayEnrollments(courseService.getStudentEnrollments(student.getId()), courseService.getAllCourses());
                    System.out.printf("%n>>> Cumulative GPA (CGPA): %.2f | Earned Credits: %d <<<%n",
                            student.getCgpa(), student.getEarnedCredits());
                }
                case 5 -> handleStudentAttendance(student);
                case 6 -> {
                    ScholarshipStrategy strategy = student.getCgpa() >= 8.5
                            ? new ScholarshipStrategy.MeritScholarshipStrategy()
                            : new ScholarshipStrategy.EarlyBirdScholarshipStrategy();
                    System.out.println(feeService.generateFeeInvoice(student, strategy));
                }
                case 7 -> {
                    authService.logout();
                    AnsiUtil.printInfo("Student logged out successfully.");
                    return;
                }
            }
        }
    }

    private void handleUserRegistration() {
        System.out.println("Select Role: 1. Student  2. Faculty  3. Admin");
        int r = view.readInt("Role Choice", 1, 3);
        Role role = switch (r) {
            case 1 -> Role.STUDENT;
            case 2 -> Role.FACULTY;
            default -> Role.ADMIN;
        };

        String name = view.readString("Enter Full Name");
        String email = view.readString("Enter University Email");
        String password = view.readString("Enter Initial Password");

        Map<String, String> attrs = new HashMap<>();
        if (role == Role.STUDENT) {
            attrs.put("regNo", view.readString("Registration Number (e.g. 25BAI11290)"));
            attrs.put("department", view.readString("Department (e.g. Computer Science)"));
            attrs.put("semester", String.valueOf(view.readInt("Current Semester", 1, 8)));
        } else if (role == Role.FACULTY) {
            attrs.put("employeeId", view.readString("Employee ID"));
            attrs.put("department", view.readString("Department"));
            attrs.put("designation", view.readString("Designation"));
            attrs.put("cabinRoom", view.readString("Cabin Number"));
        } else {
            attrs.put("adminDepartment", view.readString("Administrative Unit"));
            attrs.put("superAdmin", "false");
        }

        try {
            User created = authService.registerUser(role, name, email, password, attrs);
            AnsiUtil.printSuccess("User registered successfully: " + created.getId() + " (" + created.getName() + ")");
        } catch (AppException e) {
            AnsiUtil.printError("Registration Failed: " + e.getMessage());
        }
    }

    private void handleCreateCourse() {
        String code = view.readString("Enter Course Code (e.g. CSE2005)");
        String title = view.readString("Enter Course Title");
        int credits = view.readInt("Credits", 1, 5);
        String dept = view.readString("Department");
        String facultyId = view.readString("Assigned Faculty ID (e.g. U002)");
        int capacity = view.readInt("Max Seat Capacity", 10, 120);
        String desc = view.readString("Short Course Description");

        try {
            Course c = courseService.createCourse(code, title, credits, dept, facultyId, capacity, desc);
            AnsiUtil.printSuccess("Course Created: " + c.getCode() + " - " + c.getTitle());
        } catch (AppException e) {
            AnsiUtil.printError("Course creation failed: " + e.getMessage());
        }
    }

    private void handleAttendanceAudit() {
        AnsiUtil.printHeader("MANDATORY 75% ATTENDANCE AUDIT (< 75% DEBARMENT RISK)");
        var list = analyticsService.getAtRiskAttendanceRecords(75.0);
        if (list.isEmpty()) {
            AnsiUtil.printSuccess("All enrolled students meet the mandatory 75% attendance threshold!");
        } else {
            for (var item : list) {
                System.out.printf("! ALERT: %s (%s) | Course: %s | Attendance: %.1f%% (%s/%s classes)%n",
                        item.get("studentName"), item.get("studentId"), item.get("courseCode"),
                        item.get("percentage"), item.get("attended"), item.get("total"));
            }
        }
    }

    private void handleViewAuditLogs() {
        AnsiUtil.printHeader("RECENT SYSTEM & SECURITY AUDIT TRAIL");
        List<AuditLog> logs = AsyncAuditLogger.getInstance().getRecentLogs(10);
        if (logs.isEmpty()) {
            System.out.println("No recent logs found.");
        } else {
            logs.forEach(System.out::println);
        }
    }

    private void handleFacultyViewStudents() {
        String code = view.readString("Enter Course Code (e.g. CSE2001)");
        List<Enrollment> enrs = courseService.getCourseEnrollments(code);
        if (enrs.isEmpty()) {
            AnsiUtil.printInfo("No students currently enrolled in " + code);
            return;
        }
        System.out.printf("%-10s %-25s %-12s %-10s%n", "STUDENT ID", "STUDENT NAME", "TOTAL SCORE", "GRADE");
        System.out.println("-".repeat(60));
        for (Enrollment e : enrs) {
            String name = userRepo.findById(e.getStudentId()).map(User::getName).orElse("Unknown");
            System.out.printf("%-10s %-25s %-12.1f %-10s%n",
                    e.getStudentId(), name, e.getTotalScore(), e.getGrade());
        }
    }

    private void handleMarkAttendance(Faculty faculty) {
        String courseCode = view.readString("Enter Course Code");
        String studentId = view.readString("Enter Student ID (e.g. U004)");
        int pres = view.readInt("Present? (1 = Yes, 0 = Absent)", 0, 1);
        String topic = view.readString("Topic Covered");

        AttendanceRecord rec = attendanceService.recordAttendance(courseCode, studentId, LocalDate.now(), pres == 1, topic);
        double pct = attendanceService.getAttendancePercentage(studentId, courseCode);
        AnsiUtil.printSuccess(String.format("Attendance logged! Updated Percentage for %s: %.1f%%", studentId, pct));
    }

    private void handleGradeCourse() {
        String enrId = view.readString("Enter Enrollment ID (e.g. E001)");
        double assign = view.readDouble("Assignment Marks (max 30)", 0.0, 30.0);
        double quiz = view.readDouble("Quiz Marks (max 20)", 0.0, 20.0);
        double proj = view.readDouble("Flipped Project Marks (max 50)", 0.0, 50.0);

        System.out.println("Select Grading Strategy: 1. Absolute Cutoff  2. Relative Bell-Curve");
        int strat = view.readInt("Choice", 1, 2);

        try {
            courseService.gradeEnrollment(enrId, assign, quiz, proj,
                    strat == 1 ? new AbsoluteGradingStrategy() : new RelativeGradingStrategy());
            AnsiUtil.printSuccess("Grading successfully finalized for " + enrId);
        } catch (AppException e) {
            AnsiUtil.printError("Grading error: " + e.getMessage());
        }
    }

    private void handleCourseAnalytics() {
        String code = view.readString("Enter Course Code");
        var dist = analyticsService.getGradeDistribution(code);
        AnsiUtil.printHeader("GRADE DISTRIBUTION FOR " + code);
        dist.forEach((g, count) -> System.out.printf("  Grade %-12s : %d student(s)%n", g, count));
    }

    private void handleStudentEnroll(Student student) {
        view.displayCourses(courseService.getAllCourses());
        String code = view.readString("Enter Course Code to Enroll");
        try {
            Enrollment e = courseService.enrollStudent(student.getId(), code, student.getSemester());
            AnsiUtil.printSuccess("Enrolled successfully in " + code + " (Enrollment ID: " + e.getId() + ")");
        } catch (AppException ex) {
            AnsiUtil.printError("Enrollment failed: " + ex.getMessage());
        }
    }

    private void handleStudentDrop(Student student) {
        String code = view.readString("Enter Course Code to Drop");
        try {
            courseService.dropCourse(student.getId(), code);
            AnsiUtil.printSuccess("Successfully dropped " + code);
        } catch (AppException ex) {
            AnsiUtil.printError("Drop failed: " + ex.getMessage());
        }
    }

    private void handleStudentAttendance(Student student) {
        List<Enrollment> enrs = courseService.getStudentEnrollments(student.getId());
        AnsiUtil.printHeader("MY ATTENDANCE MONITOR");
        System.out.printf("%-10s %-12s %-12s %-14s %-15s%n", "COURSE", "ATTENDED", "TOTAL", "PERCENTAGE", "STATUS");
        System.out.println("-".repeat(66));
        for (Enrollment e : enrs) {
            int attended = attendanceService.getAttendedSessions(student.getId(), e.getCourseCode());
            int total = attendanceService.getTotalSessions(student.getId(), e.getCourseCode());
            double pct = attendanceService.getAttendancePercentage(student.getId(), e.getCourseCode());
            String status = pct >= 75.0 ? AnsiUtil.GREEN + "ELIGIBLE" + AnsiUtil.RESET : AnsiUtil.RED + "DEBAR RISK" + AnsiUtil.RESET;
            System.out.printf("%-10s %-12d %-12d %-13.1f%% %-15s%n",
                    e.getCourseCode(), attended, total, pct, status);
        }
        System.out.println("-".repeat(66));
    }
}
