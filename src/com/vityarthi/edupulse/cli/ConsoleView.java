package com.vityarthi.edupulse.cli;

import com.vityarthi.edupulse.model.Course;
import com.vityarthi.edupulse.model.Enrollment;
import com.vityarthi.edupulse.model.Student;
import com.vityarthi.edupulse.model.User;
import com.vityarthi.edupulse.util.AnsiUtil;

import java.util.List;
import java.util.Scanner;

/**
 * Terminal UI helper for rendering structured tables, boxes, and reading input safely.
 */
public class ConsoleView {
    private final Scanner scanner;

    public ConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readString(String prompt) {
        System.out.print(AnsiUtil.CYAN + prompt + ": " + AnsiUtil.RESET);
        return scanner.nextLine().trim();
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(AnsiUtil.CYAN + prompt + " (" + min + "-" + max + "): " + AnsiUtil.RESET);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                AnsiUtil.printError("Value must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                AnsiUtil.printError("Invalid integer. Please try again.");
            }
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(AnsiUtil.CYAN + prompt + " (" + min + "-" + max + "): " + AnsiUtil.RESET);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= min && val <= max) return val;
                AnsiUtil.printError("Value must be between " + min + " and " + max);
            } catch (NumberFormatException e) {
                AnsiUtil.printError("Invalid number. Please try again.");
            }
        }
    }

    public void displayCourses(List<Course> courses) {
        AnsiUtil.printHeader("ACADEMIC COURSE CATALOG");
        System.out.printf("%-10s %-36s %-8s %-18s %-12s%n", "CODE", "TITLE", "CREDITS", "DEPARTMENT", "SEATS (ENR/MAX)");
        System.out.println("-".repeat(88));
        for (Course c : courses) {
            String seatStatus = String.format("%d / %d", c.getEnrolledCount(), c.getCapacity());
            String title = c.getTitle().length() > 34 ? c.getTitle().substring(0, 31) + "..." : c.getTitle();
            System.out.printf("%-10s %-36s %-8d %-18s %-12s%n",
                    c.getCode(), title, c.getCredits(), c.getDepartment(), seatStatus);
        }
        System.out.println("-".repeat(88));
    }

    public void displayUsers(List<User> users) {
        AnsiUtil.printHeader("REGISTERED SYSTEM USERS");
        System.out.printf("%-6s %-10s %-24s %-30s%n", "ID", "ROLE", "NAME", "EMAIL");
        System.out.println("-".repeat(74));
        for (User u : users) {
            System.out.printf("%-6s %-10s %-24s %-30s%n",
                    u.getId(), u.getRole(), u.getName(), u.getEmail());
        }
        System.out.println("-".repeat(74));
    }

    public void displayEnrollments(List<Enrollment> enrollments, List<Course> courses) {
        AnsiUtil.printHeader("CURRENT COURSE ENROLLMENTS & GRADES");
        System.out.printf("%-8s %-10s %-10s %-12s %-12s %-10s%n", "ENR_ID", "COURSE", "STATUS", "SCORE /100", "GRADE", "POINTS");
        System.out.println("-".repeat(68));
        for (Enrollment e : enrollments) {
            String scoreStr = e.getTotalScore() < 0 ? "Pending" : String.format("%.1f", e.getTotalScore());
            String gradeStr = e.getGrade().name();
            String gpStr = e.getGrade().getGradePoint() > 0 ? String.format("%.1f", e.getGrade().getGradePoint()) : "-";
            System.out.printf("%-8s %-10s %-10s %-12s %-12s %-10s%n",
                    e.getId(), e.getCourseCode(), e.getStatus(), scoreStr, gradeStr, gpStr);
        }
        System.out.println("-".repeat(68));
    }

    public void displayLeaderboard(List<Student> students) {
        AnsiUtil.printHeader("ACADEMIC MERIT LEADERBOARD");
        System.out.printf("%-6s %-14s %-25s %-18s %-8s %-8s%n", "RANK", "REG NO", "STUDENT NAME", "DEPARTMENT", "CREDITS", "CGPA");
        System.out.println("-".repeat(82));
        int rank = 1;
        for (Student s : students) {
            System.out.printf("#%-5d %-14s %-25s %-18s %-8d %-8.2f%n",
                    rank++, s.getRegNo(), s.getName(), s.getDepartment(), s.getEarnedCredits(), s.getCgpa());
        }
        System.out.println("-".repeat(82));
    }
}
