package com.vityarthi.edupulse.pattern.strategy;

import com.vityarthi.edupulse.model.Student;

public interface ScholarshipStrategy {
    String getSchemeName();
    double calculateDiscountPercentage(Student student);

    class MeritScholarshipStrategy implements ScholarshipStrategy {
        @Override
        public String getSchemeName() {
            return "VITyarthi Academic Excellence Merit Scholarship";
        }

        @Override
        public double calculateDiscountPercentage(Student student) {
            double cgpa = student.getCgpa();
            if (cgpa >= 9.5) return 40.0;
            if (cgpa >= 9.0) return 25.0;
            if (cgpa >= 8.5) return 15.0;
            return 0.0;
        }
    }

    class EarlyBirdScholarshipStrategy implements ScholarshipStrategy {
        @Override
        public String getSchemeName() {
            return "Early Registration Privilege Scholarship";
        }

        @Override
        public double calculateDiscountPercentage(Student student) {
            return 10.0;
        }
    }
}
