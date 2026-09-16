package com.vityarthi.edupulse.service;

import com.vityarthi.edupulse.model.AttendanceRecord;
import com.vityarthi.edupulse.pattern.observer.AttendanceObserver;
import com.vityarthi.edupulse.repository.Repository;
import com.vityarthi.edupulse.util.AsyncAuditLogger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Attendance Tracking & Observer Dispatch Service.
 */
public class AttendanceService {
    private final Repository<AttendanceRecord, String> attendanceRepository;
    private final List<AttendanceObserver> observers = new ArrayList<>();
    private final AsyncAuditLogger auditLogger = AsyncAuditLogger.getInstance();

    public AttendanceService(Repository<AttendanceRecord, String> attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public void registerObserver(AttendanceObserver observer) {
        observers.add(observer);
    }

    public AttendanceRecord recordAttendance(String courseCode, String studentId, LocalDate date,
                                            boolean present, String topic) {
        String recordId = "ATT-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 100);
        AttendanceRecord record = new AttendanceRecord(recordId, courseCode, studentId, date, present, topic);
        attendanceRepository.save(record);

        // Notify observers
        int total = getTotalSessions(studentId, courseCode);
        int attended = getAttendedSessions(studentId, courseCode);
        double percentage = total == 0 ? 100.0 : ((double) attended / total) * 100.0;

        for (AttendanceObserver observer : observers) {
            observer.onAttendanceRecorded(studentId, courseCode, percentage, attended, total);
        }

        auditLogger.log(studentId, "ATTENDANCE_LOGGED", "SUCCESS",
                String.format("Course: %s | Present: %b | Topic: %s", courseCode, present, topic));

        return record;
    }

    public int getTotalSessions(String studentId, String courseCode) {
        return (int) attendanceRepository.findBy(a ->
                a.getStudentId().equals(studentId) && a.getCourseCode().equalsIgnoreCase(courseCode)
        ).size();
    }

    public int getAttendedSessions(String studentId, String courseCode) {
        return (int) attendanceRepository.findBy(a ->
                a.getStudentId().equals(studentId) &&
                a.getCourseCode().equalsIgnoreCase(courseCode) &&
                a.isPresent()
        ).size();
    }

    public double getAttendancePercentage(String studentId, String courseCode) {
        int total = getTotalSessions(studentId, courseCode);
        if (total == 0) return 100.0;
        int attended = getAttendedSessions(studentId, courseCode);
        return Math.round(((double) attended / total) * 1000.0) / 10.0;
    }

    public List<AttendanceRecord> getStudentCourseHistory(String studentId, String courseCode) {
        return attendanceRepository.findBy(a ->
                a.getStudentId().equals(studentId) && a.getCourseCode().equalsIgnoreCase(courseCode)
        );
    }
}
