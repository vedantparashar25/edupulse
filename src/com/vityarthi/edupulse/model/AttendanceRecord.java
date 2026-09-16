package com.vityarthi.edupulse.model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Daily attendance record for an individual class session.
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;
    private String courseCode;
    private String studentId;
    private LocalDate sessionDate;
    private boolean present;
    private String topicCovered;

    public AttendanceRecord(String recordId, String courseCode, String studentId,
                            LocalDate sessionDate, boolean present, String topicCovered) {
        this.recordId = recordId;
        this.courseCode = courseCode;
        this.studentId = studentId;
        this.sessionDate = sessionDate;
        this.present = present;
        this.topicCovered = topicCovered;
    }

    public String getRecordId() { return recordId; }
    public String getCourseCode() { return courseCode; }
    public String getStudentId() { return studentId; }
    public LocalDate getSessionDate() { return sessionDate; }
    public boolean isPresent() { return present; }
    public void setPresent(boolean present) { this.present = present; }
    public String getTopicCovered() { return topicCovered; }
}
