package com.vityarthi.edupulse.pattern.observer;

import com.vityarthi.edupulse.util.AnsiUtil;
import com.vityarthi.edupulse.util.AsyncAuditLogger;
import java.util.ArrayList;
import java.util.List;

public class AttendanceAlertNotifier implements AttendanceObserver {
    private final List<String> activeAlerts = new ArrayList<>();

    @Override
    public void onAttendanceRecorded(String studentId, String courseCode, double percentage, int attended, int total) {
        if (total >= 4 && percentage < 75.0) {
            String alert = String.format("[DEBARMENT WARNING] Student %s in Course %s has attendance %.1f%% (%d/%d classes)",
                    studentId, courseCode, percentage, attended, total);
            activeAlerts.add(alert);
            AnsiUtil.printWarning(alert + " -> Below mandatory 75% cutoff!");

            AsyncAuditLogger.getInstance().log(
                    studentId,
                    "ATTENDANCE_CRITICAL_ALERT",
                    "TRIGGERED",
                    String.format("Course: %s | Attendance: %.1f%%", courseCode, percentage)
            );
        }
    }

    public List<String> getActiveAlerts() {
        return new ArrayList<>(activeAlerts);
    }
}
