package com.vityarthi.edupulse.service;

import com.vityarthi.edupulse.common.Grade;
import com.vityarthi.edupulse.model.Course;
import com.vityarthi.edupulse.model.Enrollment;
import com.vityarthi.edupulse.model.Student;
import com.vityarthi.edupulse.model.User;
import com.vityarthi.edupulse.repository.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Academic Analytics using Java 8+ Streams, Lambdas & Aggregations.
 */
public class AnalyticsService {
    private final Repository<User, String> userRepository;
    private final Repository<Course, String> courseRepository;
    private final Repository<Enrollment, String> enrollmentRepository;
    private final AttendanceService attendanceService;

    public AnalyticsService(Repository<User, String> userRepository,
                            Repository<Course, String> courseRepository,
                            Repository<Enrollment, String> enrollmentRepository,
                            AttendanceService attendanceService) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceService = attendanceService;
    }

    /**
     * Recomputes CGPA for a student based on credits and grade points.
     */
    public double computeAndUpdateCGPA(String studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findBy(e ->
                e.getStudentId().equals(studentId) && e.getGrade() != Grade.NOT_GRADED
        );

        if (enrollments.isEmpty()) return 0.0;

        double totalWeightedPoints = 0.0;
        int totalCredits = 0;

        for (Enrollment e : enrollments) {
            Optional<Course> crsOpt = courseRepository.findById(e.getCourseCode());
            int credits = crsOpt.map(Course::getCredits).orElse(3);
            totalWeightedPoints += credits * e.getGrade().getGradePoint();
            totalCredits += credits;
        }

        double cgpa = totalCredits == 0 ? 0.0 : totalWeightedPoints / totalCredits;
        double roundedCgpa = Math.round(cgpa * 100.0) / 100.0;

        // Update student model
        Optional<User> uOpt = userRepository.findById(studentId);
        if (uOpt.isPresent() && uOpt.get() instanceof Student s) {
            s.setCgpa(roundedCgpa);
            s.setEarnedCredits(totalCredits);
            userRepository.save(s);
        }

        return roundedCgpa;
    }

    /**
     * Top Performers using Java Streams sorting and limits.
     */
    public List<Student> getTopPerformers(int limit) {
        return userRepository.findAll().stream()
                .filter(u -> u instanceof Student)
                .map(u -> (Student) u)
                .sorted(Comparator.comparingDouble(Student::getCgpa).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * At-Risk Attendance identification (< 75% cutoff).
     */
    public List<Map<String, Object>> getAtRiskAttendanceRecords(double thresholdPercentage) {
        List<Map<String, Object>> atRisk = new ArrayList<>();

        List<Enrollment> active = enrollmentRepository.findBy(e -> e.getGrade() == Grade.NOT_GRADED);
        for (Enrollment e : active) {
            int total = attendanceService.getTotalSessions(e.getStudentId(), e.getCourseCode());
            if (total >= 4) {
                double pct = attendanceService.getAttendancePercentage(e.getStudentId(), e.getCourseCode());
                if (pct < thresholdPercentage) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("studentId", e.getStudentId());
                    userRepository.findById(e.getStudentId()).ifPresent(u -> map.put("studentName", u.getName()));
                    map.put("courseCode", e.getCourseCode());
                    map.put("percentage", pct);
                    map.put("attended", attendanceService.getAttendedSessions(e.getStudentId(), e.getCourseCode()));
                    map.put("total", total);
                    atRisk.add(map);
                }
            }
        }
        return atRisk;
    }

    /**
     * Course Grade Distribution breakdown.
     */
    public Map<Grade, Long> getGradeDistribution(String courseCode) {
        return enrollmentRepository.findBy(e -> e.getCourseCode().equalsIgnoreCase(courseCode)).stream()
                .collect(Collectors.groupingBy(Enrollment::getGrade, Collectors.counting()));
    }
}
