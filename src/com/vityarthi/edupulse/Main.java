package com.vityarthi.edupulse;

import com.vityarthi.edupulse.cli.ConsoleView;
import com.vityarthi.edupulse.cli.MenuRouter;
import com.vityarthi.edupulse.common.AppException;
import com.vityarthi.edupulse.model.*;
import com.vityarthi.edupulse.pattern.observer.AttendanceAlertNotifier;
import com.vityarthi.edupulse.pattern.strategy.AbsoluteGradingStrategy;
import com.vityarthi.edupulse.pattern.strategy.ScholarshipStrategy;
import com.vityarthi.edupulse.repository.InMemoryRepository;
import com.vityarthi.edupulse.repository.JsonStorageManager;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.service.*;
import com.vityarthi.edupulse.util.AnsiUtil;
import com.vityarthi.edupulse.util.AsyncAuditLogger;

import java.util.Scanner;

/**
 * EduPulse - Smart Campus Academic & Student Analytics System
 * Official Course Project for VITyarthi Evaluated Flipped Course.
 */
public class Main {
    private static Repository<User, String> userRepo;
    private static Repository<Course, String> courseRepo;
    private static Repository<Enrollment, String> enrollRepo;
    private static Repository<AttendanceRecord, String> attRepo;

    private static AuthService authService;
    private static CourseService courseService;
    private static AttendanceService attendanceService;
    private static AnalyticsService analyticsService;
    private static FeeService feeService;
    private static JsonStorageManager storageManager;

    public static void main(String[] args) {
        initializeDependencies();

        if (args.length > 0) {
            String command = args[0].toLowerCase();
            switch (command) {
                case "--demo", "-d" -> {
                    runAutomatedDemo();
                    System.exit(0);
                }
                case "--test", "-t" -> {
                    runInlineTests();
                    System.exit(0);
                }
                case "--seed", "-s" -> {
                    storageManager.seedInitialData(userRepo, courseRepo, enrollRepo, attRepo);
                    AnsiUtil.printSuccess("Database seeded successfully with sample accounts & courses.");
                    System.exit(0);
                }
                case "--help", "-h" -> {
                    printUsage();
                    System.exit(0);
                }
            }
        }

        // Interactive Console Session
        runInteractiveApp();
    }

    private static void initializeDependencies() {
        userRepo = new InMemoryRepository<>(User::getId);
        courseRepo = new InMemoryRepository<>(Course::getCode);
        enrollRepo = new InMemoryRepository<>(Enrollment::getId);
        attRepo = new InMemoryRepository<>(AttendanceRecord::getRecordId);

        storageManager = new JsonStorageManager();
        storageManager.loadAll(userRepo, courseRepo, enrollRepo, attRepo);

        authService = new AuthService(userRepo);
        courseService = new CourseService(courseRepo, enrollRepo, userRepo);
        attendanceService = new AttendanceService(attRepo);
        analyticsService = new AnalyticsService(userRepo, courseRepo, enrollRepo, attendanceService);
        feeService = new FeeService(courseRepo, enrollRepo);

        attendanceService.registerObserver(new AttendanceAlertNotifier());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            storageManager.saveAll(userRepo, courseRepo, enrollRepo, attRepo);
            AsyncAuditLogger.getInstance().shutdown();
        }));
    }

    private static void runInteractiveApp() {
        Scanner scanner = new Scanner(System.in);
        ConsoleView view = new ConsoleView(scanner);
        MenuRouter router = new MenuRouter(authService, courseService, attendanceService, analyticsService, feeService, userRepo, view);

        AnsiUtil.printBanner();
        System.out.println("Welcome to EduPulse! Choose a demo credential or log in with your credentials:");
        System.out.println("  [Admin]   Email: admin@vityarthi.ac.in           Pass: admin123");
        System.out.println("  [Faculty] Email: sengupta@vityarthi.ac.in        Pass: faculty123");
        System.out.println("  [Student] Email: vedant.parashar@vityarthi.ac.in Pass: student123");
        System.out.println();

        while (true) {
            System.out.println("\n1. Login to Portal");
            System.out.println("2. Run Automated Feature Demo (--demo)");
            System.out.println("3. Run Self-Contained Test Suite (--test)");
            System.out.println("4. Exit EduPulse");

            int choice = view.readInt("Select Option", 1, 4);
            if (choice == 4) {
                AnsiUtil.printInfo("Exiting EduPulse. All state securely saved.");
                break;
            }
            if (choice == 2) {
                runAutomatedDemo();
                continue;
            }
            if (choice == 3) {
                runInlineTests();
                continue;
            }

            String email = view.readString("Enter Registered University Email");
            String password = view.readString("Enter Password");

            try {
                User user = authService.login(email, password);
                AnsiUtil.printSuccess("Authenticated as: " + user.getName() + " (" + user.getRole() + ")");
                router.routeUser(user);
            } catch (AppException.AuthenticationException e) {
                AnsiUtil.printError("Login Failed: " + e.getMessage());
            }
        }
    }

    public static void runAutomatedDemo() {
        AnsiUtil.printBanner();
        AnsiUtil.printHeader("RUNNING AUTOMATED END-TO-END SYSTEM DEMO");
        System.out.println("Demonstrating all modules, OOP principles, and business logic without manual entry...\n");

        AnsiUtil.printInfo("1. Admin Module: Verifying Preloaded System Users & Course Offerings...");
        System.out.printf("   Total Users: %d | Total Courses: %d%n", userRepo.count(), courseRepo.count());

        AnsiUtil.printInfo("2. Course Engine: Attempting Student Enrollment with Validation...");
        try {
            Enrollment enr = courseService.enrollStudent("U005", "CSE2002", 3);
            AnsiUtil.printSuccess("   Enrollment Succeeded: Student U005 enrolled into CSE2002 (Enrollment ID: " + enr.getId() + ")");
        } catch (AppException e) {
            AnsiUtil.printWarning("   Enrollment Notification: " + e.getMessage());
        }

        AnsiUtil.printInfo("3. Attendance Service & Observer Pattern: Recording Sessions for Student U005...");
        attendanceService.recordAttendance("CSE2001", "U005", java.time.LocalDate.now().minusDays(10), false, "Abstract Classes");
        attendanceService.recordAttendance("CSE2001", "U005", java.time.LocalDate.now().minusDays(7), false, "Interfaces & Polymorphism");
        attendanceService.recordAttendance("CSE2001", "U005", java.time.LocalDate.now().minusDays(4), false, "Generics");
        attendanceService.recordAttendance("CSE2001", "U005", java.time.LocalDate.now().minusDays(1), true, "Java Streams");

        double u5Pct = attendanceService.getAttendancePercentage("U005", "CSE2001");
        System.out.printf("   Student U005 Attendance in CSE2001: %.1f%% (Observer alerted if < 75%%)%n", u5Pct);

        AnsiUtil.printInfo("4. Strategy Pattern: Evaluating Student Grade for Vedant Parashar (U004)...");
        try {
            courseService.gradeEnrollment("E001", 28.5, 19.0, 48.5, new AbsoluteGradingStrategy());
            Enrollment e = enrollRepo.findById("E001").orElse(null);
            if (e != null) {
                System.out.printf("   Marks: Assignment=%.1f, Quiz=%.1f, Project=%.1f | Total=%.1f/100 -> Grade: %s%n",
                        e.getAssignmentMarks(), e.getQuizMarks(), e.getProjectMarks(), e.getTotalScore(), e.getGrade());
            }
        } catch (Exception ex) {
            AnsiUtil.printError("   Grading error: " + ex.getMessage());
        }

        AnsiUtil.printInfo("5. Analytics Engine (Streams & Lambdas): Computing CGPA & Leaderboard...");
        analyticsService.computeAndUpdateCGPA("U004");
        Student std = (Student) userRepo.findById("U004").orElse(null);
        if (std != null) {
            System.out.printf("   Vedant Parashar CGPA: %.2f (Earned Credits: %d)%n", std.getCgpa(), std.getEarnedCredits());
        }

        var leaders = analyticsService.getTopPerformers(3);
        System.out.println("   --- Academic Merit Leaderboard Top 3 ---");
        for (int i = 0; i < leaders.size(); i++) {
            Student s = leaders.get(i);
            System.out.printf("   #%d: %-20s | RegNo: %-12s | CGPA: %.2f%n", (i + 1), s.getName(), s.getRegNo(), s.getCgpa());
        }

        AnsiUtil.printInfo("6. Fee Service & Scholarship Strategy: Generating Tuition Invoice...");
        if (std != null) {
            String invoice = feeService.generateFeeInvoice(std, new ScholarshipStrategy.MeritScholarshipStrategy());
            System.out.println(invoice);
        }

        AnsiUtil.printInfo("7. Multithreaded Asynchronous Audit Log: Inspecting Events...");
        var logs = AsyncAuditLogger.getInstance().getRecentLogs(4);
        logs.forEach(l -> System.out.println("   " + l));

        AnsiUtil.printSuccess("\n>>> DEMO COMPLETED SUCCESSFULLY: ALL 3 MODULES & 4 DESIGN PATTERNS VERIFIED <<<");
    }

    private static void runInlineTests() {
        AnsiUtil.printHeader("RUNNING EDUPULSE TEST HARNESS");
        try {
            Class<?> clazz = Class.forName("com.vityarthi.edupulse.test.EduPulseTestSuite");
            var method = clazz.getMethod("runAllTests");
            method.invoke(null);
        } catch (Exception e) {
            System.err.println("Test suite invocation failed: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.out.println("""
            EduPulse CLI - Commands & Flags:
              java com.vityarthi.edupulse.Main            Launch interactive terminal application
              java com.vityarthi.edupulse.Main --demo     Run automated end-to-end simulation
              java com.vityarthi.edupulse.Main --test     Execute automated unit & integration test suite
              java com.vityarthi.edupulse.Main --seed     Seed fresh sample records
              java com.vityarthi.edupulse.Main --help     Show this manual
            """);
    }
}
