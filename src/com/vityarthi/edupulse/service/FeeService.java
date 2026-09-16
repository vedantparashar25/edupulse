package com.vityarthi.edupulse.service;

import com.vityarthi.edupulse.model.Course;
import com.vityarthi.edupulse.model.Enrollment;
import com.vityarthi.edupulse.model.Student;
import com.vityarthi.edupulse.pattern.strategy.ScholarshipStrategy;
import com.vityarthi.edupulse.repository.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Academic Tuition Fee Assessment and Scholarship Deduction Engine.
 */
public class FeeService {
    public static final double COST_PER_CREDIT = 6500.0; // INR per credit
    public static final double BASE_CAMPUS_AMENITIES_FEE = 12000.0;

    private final Repository<Course, String> courseRepository;
    private final Repository<Enrollment, String> enrollmentRepository;

    public FeeService(Repository<Course, String> courseRepository,
                      Repository<Enrollment, String> enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public String generateFeeInvoice(Student student, ScholarshipStrategy strategy) {
        List<Enrollment> enrollments = enrollmentRepository.findBy(e -> e.getStudentId().equals(student.getId()));
        int registeredCredits = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("\n================================================================\n");
        sb.append("                  V I T y a r t h i   F E E   I N V O I C E       \n");
        sb.append("================================================================\n");
        sb.append(String.format(" Student Name   : %-25s Registration No : %s\n", student.getName(), student.getRegNo()));
        sb.append(String.format(" Department     : %-25s Current CGPA    : %.2f\n", student.getDepartment(), student.getCgpa()));
        sb.append(String.format(" Invoice Date   : %-25s Invoice ID      : INV-%s-%d\n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                student.getId(), System.currentTimeMillis() % 10000));
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(" %-10s %-32s %-8s %-10s\n", "COURSE", "TITLE", "CREDITS", "AMOUNT (INR)"));
        sb.append("----------------------------------------------------------------\n");

        double tuitionTotal = 0.0;
        for (Enrollment e : enrollments) {
            Course c = courseRepository.findById(e.getCourseCode()).orElse(null);
            int credits = c != null ? c.getCredits() : 3;
            registeredCredits += credits;
            double courseFee = credits * COST_PER_CREDIT;
            tuitionTotal += courseFee;
            String title = c != null ? c.getTitle() : e.getCourseCode();
            if (title.length() > 30) title = title.substring(0, 27) + "...";
            sb.append(String.format(" %-10s %-32s %-8d INR %,10.2f\n", e.getCourseCode(), title, credits, courseFee));
        }

        double grossTotal = tuitionTotal + BASE_CAMPUS_AMENITIES_FEE;
        double discountPct = strategy.calculateDiscountPercentage(student);
        double waiverAmount = (tuitionTotal * discountPct) / 100.0;
        double netPayable = grossTotal - waiverAmount;

        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(" Total Registered Credits : %-15d Tuition Total  : INR %,10.2f\n", registeredCredits, tuitionTotal));
        sb.append(String.format(" Base Campus Facilities & Lab Amenities Fee       : INR %,10.2f\n", BASE_CAMPUS_AMENITIES_FEE));
        sb.append(String.format(" Gross Academic Assessment                       : INR %,10.2f\n", grossTotal));
        sb.append(String.format(" Applied Scholarship: %s (%.1f%%)   : -INR %,10.2f\n", strategy.getSchemeName(), discountPct, waiverAmount));
        sb.append("----------------------------------------------------------------\n");
        sb.append(String.format(" NET PAYABLE AMOUNT (INCL. TAXES)                : INR %,10.2f\n", netPayable));
        sb.append("================================================================\n");

        return sb.toString();
    }
}
