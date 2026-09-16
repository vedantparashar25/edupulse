package com.vityarthi.edupulse.service;

import com.vityarthi.edupulse.common.AppException;
import com.vityarthi.edupulse.common.EnrollmentStatus;
import com.vityarthi.edupulse.common.Grade;
import com.vityarthi.edupulse.model.Course;
import com.vityarthi.edupulse.model.Enrollment;
import com.vityarthi.edupulse.model.Student;
import com.vityarthi.edupulse.model.User;
import com.vityarthi.edupulse.pattern.strategy.GradingStrategy;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.util.AsyncAuditLogger;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Course and Academic Lifecycle Management Service.
 */
public class CourseService {
    private final Repository<Course, String> courseRepository;
    private final Repository<Enrollment, String> enrollmentRepository;
    private final Repository<User, String> userRepository;
    private final AsyncAuditLogger auditLogger = AsyncAuditLogger.getInstance();

    public CourseService(Repository<Course, String> courseRepository,
                         Repository<Enrollment, String> enrollmentRepository,
                         Repository<User, String> userRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourse(String code) {
        return courseRepository.findById(code);
    }

    public Course createCourse(String code, String title, int credits, String department,
                               String facultyId, int capacity, String description) throws AppException.ValidationException, AppException.DuplicateResourceException {
        if (code == null || code.trim().isEmpty() || credits <= 0 || capacity <= 0) {
            throw new AppException.ValidationException("Course parameters are invalid (credits & capacity must be > 0).");
        }
        if (courseRepository.existsById(code.toUpperCase())) {
            throw new AppException.DuplicateResourceException("Course with code " + code + " already exists.");
        }

        Course c = new Course(code.toUpperCase(), title, credits, department, facultyId, capacity, description);
        courseRepository.save(c);
        auditLogger.log("SYSTEM", "COURSE_CREATED", "SUCCESS", "Code: " + code + " | Title: " + title);
        return c;
    }

    public Enrollment enrollStudent(String studentId, String courseCode, int semester) throws AppException {
        Optional<User> stdOpt = userRepository.findById(studentId);
        if (stdOpt.isEmpty() || !(stdOpt.get() instanceof Student)) {
            throw new AppException.ResourceNotFoundException("Student not found: " + studentId);
        }

        Optional<Course> crsOpt = courseRepository.findById(courseCode);
        if (crsOpt.isEmpty()) {
            throw new AppException.ResourceNotFoundException("Course not found: " + courseCode);
        }

        Course course = crsOpt.get();

        // 1. Check Capacity
        if (!course.hasAvailableSeats()) {
            auditLogger.log(studentId, "ENROLL_FAILED", "REJECTED", "Course capacity full: " + courseCode);
            throw new AppException.CourseCapacityException("Course " + courseCode + " is at maximum capacity (" + course.getCapacity() + " seats).");
        }

        // 2. Check Duplicate
        boolean alreadyEnrolled = enrollmentRepository.findBy(e ->
                e.getStudentId().equals(studentId) &&
                e.getCourseCode().equalsIgnoreCase(courseCode) &&
                e.getStatus() == EnrollmentStatus.ENROLLED
        ).size() > 0;

        if (alreadyEnrolled) {
            throw new AppException.DuplicateResourceException("Student is already enrolled in " + courseCode);
        }

        // 3. Create Enrollment
        String enrollId = "E" + String.format("%04d", enrollmentRepository.count() + 1);
        Enrollment enrollment = new Enrollment(enrollId, studentId, course.getCode(), semester);
        course.incrementEnrolled();
        courseRepository.save(course);
        enrollmentRepository.save(enrollment);

        auditLogger.log(studentId, "COURSE_ENROLLED", "SUCCESS", "Course: " + course.getCode() + " | ID: " + enrollId);
        return enrollment;
    }

    public boolean dropCourse(String studentId, String courseCode) throws AppException {
        List<Enrollment> matches = enrollmentRepository.findBy(e ->
                e.getStudentId().equals(studentId) &&
                e.getCourseCode().equalsIgnoreCase(courseCode) &&
                e.getStatus() == EnrollmentStatus.ENROLLED
        );

        if (matches.isEmpty()) {
            throw new AppException.ResourceNotFoundException("No active enrollment found for " + courseCode);
        }

        Enrollment e = matches.get(0);
        e.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(e);

        Optional<Course> crsOpt = courseRepository.findById(courseCode);
        crsOpt.ifPresent(c -> {
            c.decrementEnrolled();
            courseRepository.save(c);
        });

        auditLogger.log(studentId, "COURSE_DROPPED", "SUCCESS", "Course: " + courseCode);
        return true;
    }

    public List<Enrollment> getStudentEnrollments(String studentId) {
        return enrollmentRepository.findBy(e -> e.getStudentId().equals(studentId));
    }

    public List<Enrollment> getCourseEnrollments(String courseCode) {
        return enrollmentRepository.findBy(e -> e.getCourseCode().equalsIgnoreCase(courseCode));
    }

    public void gradeEnrollment(String enrollmentId, double assignment, double quiz, double project,
                                GradingStrategy strategy) throws AppException {
        Optional<Enrollment> opt = enrollmentRepository.findById(enrollmentId);
        if (opt.isEmpty()) {
            throw new AppException.ResourceNotFoundException("Enrollment record not found: " + enrollmentId);
        }

        if (assignment < 0 || assignment > 30 || quiz < 0 || quiz > 20 || project < 0 || project > 50) {
            throw new AppException.ValidationException("Marks exceed bounds (Assignment: 0-30, Quiz: 0-20, Project: 0-50).");
        }

        Enrollment e = opt.get();
        e.setAssignmentMarks(assignment);
        e.setQuizMarks(quiz);
        e.setProjectMarks(project);
        e.recalculateScore();

        List<Double> cohortScores = getCourseEnrollments(e.getCourseCode()).stream()
                .filter(en -> en.getTotalScore() >= 0)
                .map(Enrollment::getTotalScore)
                .collect(Collectors.toList());

        Grade determinedGrade = strategy.evaluateGrade(e.getTotalScore(), cohortScores);
        e.setGrade(determinedGrade);
        e.setStatus(EnrollmentStatus.COMPLETED);
        enrollmentRepository.save(e);

        auditLogger.log("FACULTY", "GRADE_RECORDED", "SUCCESS",
                String.format("Enrollment: %s | Score: %.1f | Grade: %s", enrollmentId, e.getTotalScore(), determinedGrade));
    }
}
