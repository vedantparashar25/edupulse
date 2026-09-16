package com.vityarthi.edupulse.repository;

import com.vityarthi.edupulse.common.EnrollmentStatus;
import com.vityarthi.edupulse.common.Grade;
import com.vityarthi.edupulse.common.Role;
import com.vityarthi.edupulse.model.*;
import com.vityarthi.edupulse.util.SecurityUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Storage Manager handling persistent file synchronization for EduPulse entities.
 * Supports CSV/JSON tabular record format for zero external dependencies.
 */
public class JsonStorageManager {
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + "/users.csv";
    private static final String COURSES_FILE = DATA_DIR + "/courses.csv";
    private static final String ENROLLMENTS_FILE = DATA_DIR + "/enrollments.csv";
    private static final String ATTENDANCE_FILE = DATA_DIR + "/attendance.csv";

    public JsonStorageManager() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    // ==========================================
    // Seed Sample Data If Fresh
    // ==========================================
    public void seedInitialData(Repository<User, String> userRepo,
                               Repository<Course, String> courseRepo,
                               Repository<Enrollment, String> enrollRepo,
                               Repository<AttendanceRecord, String> attRepo) {

        if (userRepo.count() > 0) return; // already loaded

        // 1. Admin
        String saltAdmin = SecurityUtil.generateSalt();
        String passAdmin = SecurityUtil.hashPassword("admin123", saltAdmin);
        Admin admin = new Admin("U001", "Dr. A. Sharma", "admin@vityarthi.ac.in", passAdmin, saltAdmin, "Academic Affairs", true);
        userRepo.save(admin);

        // 2. Faculty
        String saltFac1 = SecurityUtil.generateSalt();
        String passFac1 = SecurityUtil.hashPassword("faculty123", saltFac1);
        Faculty fac1 = new Faculty("U002", "Prof. R. Sengupta", "sengupta@vityarthi.ac.in", passFac1, saltFac1, "EMP-101", "Computer Science", "Associate Professor", "AB1-404");
        userRepo.save(fac1);

        String saltFac2 = SecurityUtil.generateSalt();
        String passFac2 = SecurityUtil.hashPassword("faculty123", saltFac2);
        Faculty fac2 = new Faculty("U003", "Dr. Neha Verma", "neha.verma@vityarthi.ac.in", passFac2, saltFac2, "EMP-102", "Software Systems", "Professor", "AB2-205");
        userRepo.save(fac2);

        // 3. Students
        String saltStd1 = SecurityUtil.generateSalt();
        String passStd1 = SecurityUtil.hashPassword("student123", saltStd1);
        Student std1 = new Student("U004", "Vedant Parashar", "vedant.parashar@vityarthi.ac.in", passStd1, saltStd1, "25BAI11290", "Computer Science", 3);
        std1.setCgpa(9.25);
        std1.setEarnedCredits(48);
        userRepo.save(std1);

        String saltStd2 = SecurityUtil.generateSalt();
        String passStd2 = SecurityUtil.hashPassword("student123", saltStd2);
        Student std2 = new Student("U005", "Aarav Patel", "aarav.patel@vityarthi.ac.in", passStd2, saltStd2, "25BAI11015", "Computer Science", 3);
        std2.setCgpa(8.60);
        std2.setEarnedCredits(44);
        userRepo.save(std2);

        String saltStd3 = SecurityUtil.generateSalt();
        String passStd3 = SecurityUtil.hashPassword("student123", saltStd3);
        Student std3 = new Student("U006", "Ananya Iyer", "ananya.iyer@vityarthi.ac.in", passStd3, saltStd3, "25BAI11088", "Data Science", 3);
        std3.setCgpa(9.65);
        std3.setEarnedCredits(52);
        userRepo.save(std3);

        // 4. Courses
        Course c1 = new Course("CSE2001", "Object-Oriented Programming with Java", 4, "Computer Science", "U002", 60, "Core OOP concepts, streams, concurrency, design patterns.");
        Course c2 = new Course("CSE2002", "Data Structures and Algorithms", 4, "Computer Science", "U002", 60, "Linear and non-linear data structures, trees, graphs, sorting.");
        Course c3 = new Course("CSE3001", "Database Systems & Design", 3, "Software Systems", "U003", 50, "Relational modeling, SQL, indexing, transaction ACID properties.");
        Course c4 = new Course("MAT2001", "Discrete Mathematics & Graph Theory", 3, "Mathematics", "U003", 70, "Logic, set theory, graph theory, combinatorics.");

        courseRepo.save(c1);
        courseRepo.save(c2);
        courseRepo.save(c3);
        courseRepo.save(c4);

        // 5. Enrollments
        Enrollment e1 = new Enrollment("E001", "U004", "CSE2001", 3);
        e1.setAssignmentMarks(28.0);
        e1.setQuizMarks(18.5);
        e1.setProjectMarks(48.0);
        e1.recalculateScore();
        e1.setGrade(Grade.S);
        c1.incrementEnrolled();
        enrollRepo.save(e1);

        Enrollment e2 = new Enrollment("E002", "U004", "CSE2002", 3);
        e2.setAssignmentMarks(25.0);
        e2.setQuizMarks(17.0);
        e2.setProjectMarks(42.0);
        e2.recalculateScore();
        e2.setGrade(Grade.A);
        c2.incrementEnrolled();
        enrollRepo.save(e2);

        Enrollment e3 = new Enrollment("E003", "U005", "CSE2001", 3);
        e3.setAssignmentMarks(24.0);
        e3.setQuizMarks(16.0);
        e3.setProjectMarks(41.0);
        e3.recalculateScore();
        e3.setGrade(Grade.B);
        c1.incrementEnrolled();
        enrollRepo.save(e3);

        Enrollment e4 = new Enrollment("E004", "U006", "CSE2001", 3);
        e4.setAssignmentMarks(29.5);
        e4.setQuizMarks(19.0);
        e4.setProjectMarks(49.0);
        e4.recalculateScore();
        e4.setGrade(Grade.S);
        c1.incrementEnrolled();
        enrollRepo.save(e4);

        // 6. Attendance records
        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 1; i--) {
            LocalDate d = today.minusDays(i * 3L);
            attRepo.save(new AttendanceRecord("ATT-" + i + "-1", "CSE2001", "U004", d, true, "Unit " + (6 - i) + " Theory"));
            attRepo.save(new AttendanceRecord("ATT-" + i + "-2", "CSE2001", "U005", d, i != 3, "Unit " + (6 - i) + " Theory"));
            attRepo.save(new AttendanceRecord("ATT-" + i + "-3", "CSE2001", "U006", d, true, "Unit " + (6 - i) + " Theory"));
        }

        saveAll(userRepo, courseRepo, enrollRepo, attRepo);
    }

    public synchronized void saveAll(Repository<User, String> userRepo,
                                     Repository<Course, String> courseRepo,
                                     Repository<Enrollment, String> enrollRepo,
                                     Repository<AttendanceRecord, String> attRepo) {
        saveUsers(userRepo.findAll());
        saveCourses(courseRepo.findAll());
        saveEnrollments(enrollRepo.findAll());
        saveAttendance(attRepo.findAll());
    }

    private void saveUsers(List<User> users) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (User u : users) {
                if (u instanceof Student s) {
                    bw.write(String.format("STUDENT,%s,%s,%s,%s,%s,%s,%s,%d,%.2f,%d,%.2f\n",
                            escape(s.getId()), escape(s.getName()), escape(s.getEmail()),
                            s.getPasswordHash(), s.getSalt(), escape(s.getRegNo()),
                            s.getDepartment(), s.getSemester(), s.getCgpa(), s.getEarnedCredits(),
                            s.getScholarshipPercentage()));
                } else if (u instanceof Faculty f) {
                    bw.write(String.format("FACULTY,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                            escape(f.getId()), escape(f.getName()), escape(f.getEmail()),
                            f.getPasswordHash(), f.getSalt(), escape(f.getEmployeeId()),
                            escape(f.getDepartment()), escape(f.getDesignation()), escape(f.getCabinRoom())));
                } else if (u instanceof Admin a) {
                    bw.write(String.format("ADMIN,%s,%s,%s,%s,%s,%s,%b\n",
                            escape(a.getId()), escape(a.getName()), escape(a.getEmail()),
                            a.getPasswordHash(), a.getSalt(), escape(a.getAdminDepartment()),
                            a.isSuperAdmin()));
                }
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    private void saveCourses(List<Course> courses) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(COURSES_FILE))) {
            for (Course c : courses) {
                String prereqs = String.join(";", c.getPrerequisites());
                bw.write(String.format("%s,%s,%d,%s,%s,%d,%d,%s,%s\n",
                        escape(c.getCode()), escape(c.getTitle()), c.getCredits(),
                        escape(c.getDepartment()), escape(c.getFacultyId()),
                        c.getCapacity(), c.getEnrolledCount(), escape(prereqs),
                        escape(c.getDescription())));
            }
        } catch (IOException e) {
            System.err.println("Error saving courses: " + e.getMessage());
        }
    }

    private void saveEnrollments(List<Enrollment> enrollments) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ENROLLMENTS_FILE))) {
            for (Enrollment e : enrollments) {
                bw.write(String.format("%s,%s,%s,%d,%s,%.2f,%.2f,%.2f,%.2f,%s\n",
                        escape(e.getId()), escape(e.getStudentId()), escape(e.getCourseCode()),
                        e.getSemester(), e.getStatus().name(), e.getAssignmentMarks(),
                        e.getQuizMarks(), e.getProjectMarks(), e.getTotalScore(),
                        e.getGrade().name()));
            }
        } catch (IOException e) {
            System.err.println("Error saving enrollments: " + e.getMessage());
        }
    }

    private void saveAttendance(List<AttendanceRecord> records) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ATTENDANCE_FILE))) {
            for (AttendanceRecord a : records) {
                bw.write(String.format("%s,%s,%s,%s,%b,%s\n",
                        escape(a.getRecordId()), escape(a.getCourseCode()),
                        escape(a.getStudentId()), a.getSessionDate().toString(),
                        a.isPresent(), escape(a.getTopicCovered())));
            }
        } catch (IOException e) {
            System.err.println("Error saving attendance: " + e.getMessage());
        }
    }

    public void loadAll(Repository<User, String> userRepo,
                        Repository<Course, String> courseRepo,
                        Repository<Enrollment, String> enrollRepo,
                        Repository<AttendanceRecord, String> attRepo) {

        loadUsers(userRepo);
        loadCourses(courseRepo);
        loadEnrollments(enrollRepo);
        loadAttendance(attRepo);

        if (userRepo.count() == 0) {
            seedInitialData(userRepo, courseRepo, enrollRepo, attRepo);
        }
    }

    private void loadUsers(Repository<User, String> repo) {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                String type = parts[0];
                if ("STUDENT".equals(type) && parts.length >= 11) {
                    Student s = new Student(parts[1], parts[2], parts[3], parts[4], parts[5],
                            parts[6], parts[7], Integer.parseInt(parts[8]));
                    s.setCgpa(Double.parseDouble(parts[9]));
                    s.setEarnedCredits(Integer.parseInt(parts[10]));
                    if (parts.length > 11) s.setScholarshipPercentage(Double.parseDouble(parts[11]));
                    repo.save(s);
                } else if ("FACULTY".equals(type) && parts.length >= 10) {
                    Faculty f = new Faculty(parts[1], parts[2], parts[3], parts[4], parts[5],
                            parts[6], parts[7], parts[8], parts[9]);
                    repo.save(f);
                } else if ("ADMIN".equals(type) && parts.length >= 8) {
                    Admin a = new Admin(parts[1], parts[2], parts[3], parts[4], parts[5],
                            parts[6], Boolean.parseBoolean(parts[7]));
                    repo.save(a);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void loadCourses(Repository<Course, String> repo) {
        File file = new File(COURSES_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length >= 9) {
                    List<String> prereqs = p[7].isEmpty() ? new ArrayList<>() : Arrays.asList(p[7].split(";"));
                    Course c = new Course(p[0], p[1], Integer.parseInt(p[2]), p[3], p[4],
                            Integer.parseInt(p[5]), Integer.parseInt(p[6]), prereqs, p[8]);
                    repo.save(c);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading courses: " + e.getMessage());
        }
    }

    private void loadEnrollments(Repository<Enrollment, String> repo) {
        File file = new File(ENROLLMENTS_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length >= 10) {
                    Enrollment e = new Enrollment(p[0], p[1], p[2], Integer.parseInt(p[3]),
                            EnrollmentStatus.valueOf(p[4]), Double.parseDouble(p[5]),
                            Double.parseDouble(p[6]), Double.parseDouble(p[7]),
                            Double.parseDouble(p[8]), Grade.valueOf(p[9]), LocalDateTime.now());
                    repo.save(e);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading enrollments: " + e.getMessage());
        }
    }

    private void loadAttendance(Repository<AttendanceRecord, String> repo) {
        File file = new File(ATTENDANCE_FILE);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",", -1);
                if (p.length >= 6) {
                    AttendanceRecord a = new AttendanceRecord(p[0], p[1], p[2],
                            LocalDate.parse(p[3]), Boolean.parseBoolean(p[4]), p[5]);
                    repo.save(a);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading attendance: " + e.getMessage());
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace(",", " ").trim();
    }
}
