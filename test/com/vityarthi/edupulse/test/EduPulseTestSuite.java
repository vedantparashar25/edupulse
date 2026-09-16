package com.vityarthi.edupulse.test;

import com.vityarthi.edupulse.common.AppException;
import com.vityarthi.edupulse.common.Grade;
import com.vityarthi.edupulse.common.Role;
import com.vityarthi.edupulse.model.*;
import com.vityarthi.edupulse.pattern.factory.UserFactory;
import com.vityarthi.edupulse.pattern.observer.AttendanceAlertNotifier;
import com.vityarthi.edupulse.pattern.strategy.AbsoluteGradingStrategy;
import com.vityarthi.edupulse.pattern.strategy.RelativeGradingStrategy;
import com.vityarthi.edupulse.pattern.strategy.ScholarshipStrategy;
import com.vityarthi.edupulse.repository.InMemoryRepository;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.service.*;
import com.vityarthi.edupulse.util.AnsiUtil;
import com.vityarthi.edupulse.util.AsyncAuditLogger;
import com.vityarthi.edupulse.util.SecurityUtil;

import java.time.LocalDate;
import java.util.*;

/**
 * Self-contained Automated Test Suite for EduPulse.
 * Executes 16 comprehensive unit and integration assertions across all layers.
 */
public class EduPulseTestSuite {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        runAllTests();
    }

    public static void runAllTests() {
        totalTests = 0;
        passedTests = 0;

        AnsiUtil.printBanner();
        AnsiUtil.printHeader("EDUPULSE AUTOMATED UNIT & INTEGRATION TEST SUITE");

        testSecurityHashing();
        testUserFactoryCreation();
        testAuthenticationWorkflow();
        testCourseCreationValidation();
        testCourseCapacityLimit();
        testDuplicateEnrollmentPrevention();
        testAttendanceTrackingCalculation();
        testAttendanceObserverDebarmentAlert();
        testAbsoluteGradingStrategy();
        testRelativeGradingStrategy();
        testCGPAComputation();
        testScholarshipStrategy();
        testDropCourseCapacityRestoration();
        testTopPerformersStreamAnalytics();
        testAsyncAuditLogger();
        testAtRiskAttendanceQuery();

        System.out.println("\n========================================================");
        if (passedTests == totalTests) {
            AnsiUtil.printSuccess(String.format("ALL TESTS PASSED: %d / %d assertions verified (100%%)", passedTests, totalTests));
        } else {
            AnsiUtil.printError(String.format("TEST FAILURES OCCURRED: %d passed, %d failed", passedTests, (totalTests - passedTests)));
        }
        System.out.println("========================================================\n");
    }

    private static void assertEquals(String testName, Object expected, Object actual) {
        totalTests++;
        if (Objects.equals(expected, actual)) {
            passedTests++;
            System.out.printf("  [PASS] %s%n", testName);
        } else {
            System.err.printf("  [FAIL] %s - Expected: %s, Got: %s%n", testName, expected, actual);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %s%n", testName);
        } else {
            System.err.printf("  [FAIL] %s - Condition expected true but was false%n", testName);
        }
    }

    private static void testSecurityHashing() {
        String salt = SecurityUtil.generateSalt();
        String hash1 = SecurityUtil.hashPassword("secret123", salt);
        String hash2 = SecurityUtil.hashPassword("secret123", salt);
        assertEquals("SHA-256 Deterministic Hash Match", hash1, hash2);
        assertTrue("Password verification succeeds for correct password",
                SecurityUtil.verifyPassword("secret123", salt, hash1));
        assertTrue("Password verification fails for incorrect password",
                !SecurityUtil.verifyPassword("wrongpass", salt, hash1));
    }

    private static void testUserFactoryCreation() {
        Map<String, String> attrs = new HashMap<>();
        attrs.put("regNo", "25BAI99999");
        attrs.put("department", "Computer Science");
        attrs.put("semester", "2");

        User u = UserFactory.createUser(Role.STUDENT, "U999", "Test Student", "test@vityarthi.ac.in", "pass", attrs);
        assertTrue("Factory produces Student instance", u instanceof Student);
        Student s = (Student) u;
        assertEquals("Student regNo matches", "25BAI99999", s.getRegNo());
        assertEquals("Student semester matches", 2, s.getSemester());
    }

    private static void testAuthenticationWorkflow() {
        Repository<User, String> repo = new InMemoryRepository<>(User::getId);
        AuthService auth = new AuthService(repo);

        Map<String, String> attrs = new HashMap<>();
        attrs.put("regNo", "25BAI11111");
        try {
            auth.registerUser(Role.STUDENT, "Auth Tester", "authtest@vityarthi.ac.in", "correctpass", attrs);
            User logged = auth.login("authtest@vityarthi.ac.in", "correctpass");
            assertEquals("Auth login returns correct user", "authtest@vityarthi.ac.in", logged.getEmail());
            assertTrue("User is marked authenticated", auth.isAuthenticated());
        } catch (Exception e) {
            assertTrue("Authentication workflow threw unexpected exception: " + e.getMessage(), false);
        }
    }

    private static void testCourseCreationValidation() {
        Repository<Course, String> crsRepo = new InMemoryRepository<>(Course::getCode);
        CourseService cs = new CourseService(crsRepo, new InMemoryRepository<>(Enrollment::getId), new InMemoryRepository<>(User::getId));

        try {
            cs.createCourse("CSE9001", "Cloud Computing", 4, "CSE", "U002", 40, "Cloud infra");
            assertTrue("Course CSE9001 created", crsRepo.existsById("CSE9001"));
        } catch (Exception e) {
            assertTrue("Course creation exception: " + e.getMessage(), false);
        }
    }

    private static void testCourseCapacityLimit() {
        Repository<Course, String> crsRepo = new InMemoryRepository<>(Course::getCode);
        Repository<Enrollment, String> enrRepo = new InMemoryRepository<>(Enrollment::getId);
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        CourseService cs = new CourseService(crsRepo, enrRepo, userRepo);

        Course c = new Course("TINY101", "Tiny Class", 2, "CSE", "U002", 1, "Max 1 student");
        crsRepo.save(c);

        Student s1 = new Student("U101", "Std 1", "s1@vit.in", "p", "s", "R1", "CSE", 1);
        Student s2 = new Student("U102", "Std 2", "s2@vit.in", "p", "s", "R2", "CSE", 1);
        userRepo.save(s1);
        userRepo.save(s2);

        try {
            cs.enrollStudent("U101", "TINY101", 1);
            boolean threwCapacity = false;
            try {
                cs.enrollStudent("U102", "TINY101", 1);
            } catch (AppException.CourseCapacityException ex) {
                threwCapacity = true;
            }
            assertTrue("CourseCapacityException thrown when capacity exceeded", threwCapacity);
        } catch (Exception e) {
            assertTrue("Unexpected error in capacity test: " + e.getMessage(), false);
        }
    }

    private static void testDuplicateEnrollmentPrevention() {
        Repository<Course, String> crsRepo = new InMemoryRepository<>(Course::getCode);
        Repository<Enrollment, String> enrRepo = new InMemoryRepository<>(Enrollment::getId);
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        CourseService cs = new CourseService(crsRepo, enrRepo, userRepo);

        Course c = new Course("DUP101", "Dup Class", 3, "CSE", "U002", 10, "Desc");
        crsRepo.save(c);

        Student s = new Student("U201", "Dup Tester", "dup@vit.in", "p", "s", "RD1", "CSE", 1);
        userRepo.save(s);

        try {
            cs.enrollStudent("U201", "DUP101", 1);
            boolean caughtDup = false;
            try {
                cs.enrollStudent("U201", "DUP101", 1);
            } catch (AppException.DuplicateResourceException ex) {
                caughtDup = true;
            }
            assertTrue("Duplicate enrollment prevented", caughtDup);
        } catch (Exception e) {
            assertTrue("Unexpected error in duplicate test", false);
        }
    }

    private static void testAttendanceTrackingCalculation() {
        Repository<AttendanceRecord, String> attRepo = new InMemoryRepository<>(AttendanceRecord::getRecordId);
        AttendanceService attService = new AttendanceService(attRepo);

        attService.recordAttendance("CSE101", "U301", LocalDate.now().minusDays(3), true, "Topic 1");
        attService.recordAttendance("CSE101", "U301", LocalDate.now().minusDays(2), false, "Topic 2");
        attService.recordAttendance("CSE101", "U301", LocalDate.now().minusDays(1), true, "Topic 3");
        attService.recordAttendance("CSE101", "U301", LocalDate.now(), true, "Topic 4");

        assertEquals("Total sessions recorded", 4, attService.getTotalSessions("U301", "CSE101"));
        assertEquals("Attended sessions counted", 3, attService.getAttendedSessions("U301", "CSE101"));
        assertEquals("Attendance percentage calculated", 75.0, attService.getAttendancePercentage("U301", "CSE101"));
    }

    private static void testAttendanceObserverDebarmentAlert() {
        Repository<AttendanceRecord, String> attRepo = new InMemoryRepository<>(AttendanceRecord::getRecordId);
        AttendanceService attService = new AttendanceService(attRepo);
        AttendanceAlertNotifier notifier = new AttendanceAlertNotifier();
        attService.registerObserver(notifier);

        attService.recordAttendance("CSE200", "U401", LocalDate.now().minusDays(4), true, "T1");
        attService.recordAttendance("CSE200", "U401", LocalDate.now().minusDays(3), false, "T2");
        attService.recordAttendance("CSE200", "U401", LocalDate.now().minusDays(2), false, "T3");
        attService.recordAttendance("CSE200", "U401", LocalDate.now().minusDays(1), true, "T4");

        assertTrue("Attendance observer raised debarment alert for < 75%", !notifier.getActiveAlerts().isEmpty());
    }

    private static void testAbsoluteGradingStrategy() {
        AbsoluteGradingStrategy strat = new AbsoluteGradingStrategy();
        assertEquals("Score 94 -> Grade S", Grade.S, strat.evaluateGrade(94.0, null));
        assertEquals("Score 84 -> Grade A", Grade.A, strat.evaluateGrade(84.0, null));
        assertEquals("Score 74 -> Grade B", Grade.B, strat.evaluateGrade(74.0, null));
        assertEquals("Score 64 -> Grade C", Grade.C, strat.evaluateGrade(64.0, null));
        assertEquals("Score 54 -> Grade D", Grade.D, strat.evaluateGrade(54.0, null));
        assertEquals("Score 44 -> Grade E", Grade.E, strat.evaluateGrade(44.0, null));
        assertEquals("Score 32 -> Grade F", Grade.F, strat.evaluateGrade(32.0, null));
    }

    private static void testRelativeGradingStrategy() {
        RelativeGradingStrategy strat = new RelativeGradingStrategy();
        List<Double> cohort = List.of(50.0, 60.0, 70.0, 80.0, 90.0);
        Grade gHigh = strat.evaluateGrade(92.0, cohort);
        assertTrue("High score in cohort receives top tier grade", gHigh == Grade.S || gHigh == Grade.A);
    }

    private static void testCGPAComputation() {
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        Repository<Course, String> crsRepo = new InMemoryRepository<>(Course::getCode);
        Repository<Enrollment, String> enrRepo = new InMemoryRepository<>(Enrollment::getId);
        AnalyticsService analytics = new AnalyticsService(userRepo, crsRepo, enrRepo, new AttendanceService(new InMemoryRepository<>(AttendanceRecord::getRecordId)));

        Student s = new Student("U501", "GPA Student", "gpa@vit.in", "p", "s", "RGPA", "CSE", 2);
        userRepo.save(s);

        crsRepo.save(new Course("C1", "Course 1", 4, "CSE", "F1", 50, ""));
        crsRepo.save(new Course("C2", "Course 2", 3, "CSE", "F1", 50, ""));

        Enrollment e1 = new Enrollment("E1", "U501", "C1", 2);
        e1.setGrade(Grade.S);
        enrRepo.save(e1);

        Enrollment e2 = new Enrollment("E2", "U501", "C2", 2);
        e2.setGrade(Grade.A);
        enrRepo.save(e2);

        double cgpa = analytics.computeAndUpdateCGPA("U501");
        assertEquals("Weighted CGPA calculation", 9.57, cgpa);
    }

    private static void testScholarshipStrategy() {
        Student highAchiever = new Student("US1", "Scholar", "sc@vit.in", "p", "s", "RSC", "CSE", 3);
        highAchiever.setCgpa(9.6);

        ScholarshipStrategy merit = new ScholarshipStrategy.MeritScholarshipStrategy();
        double discount = merit.calculateDiscountPercentage(highAchiever);
        assertEquals("CGPA >= 9.5 receives 40% scholarship", 40.0, discount);
    }

    private static void testDropCourseCapacityRestoration() {
        Repository<Course, String> crsRepo = new InMemoryRepository<>(Course::getCode);
        Repository<Enrollment, String> enrRepo = new InMemoryRepository<>(Enrollment::getId);
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        CourseService cs = new CourseService(crsRepo, enrRepo, userRepo);

        Course c = new Course("DROP101", "Drop Course", 3, "CSE", "F1", 10, "");
        crsRepo.save(c);

        Student s = new Student("U601", "Drop Student", "ds@vit.in", "p", "s", "RDROP", "CSE", 1);
        userRepo.save(s);

        try {
            cs.enrollStudent("U601", "DROP101", 1);
            assertEquals("Course enrolled count after enroll", 1, crsRepo.findById("DROP101").get().getEnrolledCount());

            cs.dropCourse("U601", "DROP101");
            assertEquals("Course enrolled count after drop", 0, crsRepo.findById("DROP101").get().getEnrolledCount());
        } catch (Exception e) {
            assertTrue("Drop course exception: " + e.getMessage(), false);
        }
    }

    private static void testTopPerformersStreamAnalytics() {
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        AnalyticsService analytics = new AnalyticsService(userRepo, null, null, null);

        Student s1 = new Student("ST1", "S1", "s1@v.in", "p", "s", "R1", "CSE", 1);
        s1.setCgpa(8.2);
        Student s2 = new Student("ST2", "S2", "s2@v.in", "p", "s", "R2", "CSE", 1);
        s2.setCgpa(9.8);
        Student s3 = new Student("ST3", "S3", "s3@v.in", "p", "s", "R3", "CSE", 1);
        s3.setCgpa(9.1);

        userRepo.save(s1);
        userRepo.save(s2);
        userRepo.save(s3);

        List<Student> top = analytics.getTopPerformers(2);
        assertEquals("Top 1 performer is highest CGPA", "ST2", top.get(0).getId());
        assertEquals("Top 2 performer is second highest", "ST3", top.get(1).getId());
    }

    private static void testAsyncAuditLogger() {
        AsyncAuditLogger logger = AsyncAuditLogger.getInstance();
        logger.log("TEST_USER", "TEST_ACTION", "OK", "Sample audit record");
        assertTrue("Audit log recorded in memory", logger.getRecentLogs(5).size() > 0);
    }

    private static void testAtRiskAttendanceQuery() {
        Repository<User, String> userRepo = new InMemoryRepository<>(User::getId);
        Repository<Enrollment, String> enrRepo = new InMemoryRepository<>(Enrollment::getId);
        Repository<AttendanceRecord, String> attRepo = new InMemoryRepository<>(AttendanceRecord::getRecordId);
        AttendanceService attService = new AttendanceService(attRepo);
        AnalyticsService analytics = new AnalyticsService(userRepo, null, enrRepo, attService);

        Student s = new Student("U701", "Risk Student", "risk@v.in", "p", "s", "RRISK", "CSE", 1);
        userRepo.save(s);

        Enrollment e = new Enrollment("ERISK", "U701", "RISK101", 1);
        enrRepo.save(e);

        attService.recordAttendance("RISK101", "U701", LocalDate.now().minusDays(4), true, "T1");
        attService.recordAttendance("RISK101", "U701", LocalDate.now().minusDays(3), false, "T2");
        attService.recordAttendance("RISK101", "U701", LocalDate.now().minusDays(2), false, "T3");
        attService.recordAttendance("RISK101", "U701", LocalDate.now().minusDays(1), false, "T4");

        var atRisk = analytics.getAtRiskAttendanceRecords(75.0);
        assertTrue("At-risk student identified", atRisk.stream().anyMatch(m -> m.get("studentId").equals("U701")));
    }
}
