package com.vityarthi.edupulse.model;

import com.vityarthi.edupulse.common.EnrollmentStatus;
import com.vityarthi.edupulse.common.Grade;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a student's registration in a course, carrying grading state.
 */
public class Enrollment implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String studentId;
    private String courseCode;
    private int semester;
    private EnrollmentStatus status;
    private double assignmentMarks; // out of 30
    private double quizMarks;       // out of 20
    private double projectMarks;    // out of 50 (Flipped course evaluated project)
    private double totalScore;      // out of 100
    private Grade grade;
    private LocalDateTime enrolledAt;

    public Enrollment(String id, String studentId, String courseCode, int semester) {
        this.id = id;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.semester = semester;
        this.status = EnrollmentStatus.ENROLLED;
        this.assignmentMarks = 0.0;
        this.quizMarks = 0.0;
        this.projectMarks = 0.0;
        this.totalScore = -1.0;
        this.grade = Grade.NOT_GRADED;
        this.enrolledAt = LocalDateTime.now();
    }

    public Enrollment(String id, String studentId, String courseCode, int semester,
                      EnrollmentStatus status, double assignmentMarks, double quizMarks,
                      double projectMarks, double totalScore, Grade grade, LocalDateTime enrolledAt) {
        this.id = id;
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.semester = semester;
        this.status = status;
        this.assignmentMarks = assignmentMarks;
        this.quizMarks = quizMarks;
        this.projectMarks = projectMarks;
        this.totalScore = totalScore;
        this.grade = grade;
        this.enrolledAt = enrolledAt != null ? enrolledAt : LocalDateTime.now();
    }

    public void recalculateScore() {
        this.totalScore = this.assignmentMarks + this.quizMarks + this.projectMarks;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getCourseCode() { return courseCode; }
    public int getSemester() { return semester; }
    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
    public double getAssignmentMarks() { return assignmentMarks; }
    public void setAssignmentMarks(double assignmentMarks) { this.assignmentMarks = assignmentMarks; }
    public double getQuizMarks() { return quizMarks; }
    public void setQuizMarks(double quizMarks) { this.quizMarks = quizMarks; }
    public double getProjectMarks() { return projectMarks; }
    public void setProjectMarks(double projectMarks) { this.projectMarks = projectMarks; }
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public LocalDateTime getEnrolledAt() { return enrolledAt; }
}
