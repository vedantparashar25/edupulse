package com.vityarthi.edupulse.pattern.observer;

public interface AttendanceObserver {
    void onAttendanceRecorded(String studentId, String courseCode, double percentage, int attended, int total);
}
