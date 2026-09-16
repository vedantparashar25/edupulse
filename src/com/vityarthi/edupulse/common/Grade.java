package com.vityarthi.edupulse.common;

/**
 * Standard 10-point scale grading system followed in flipped course evaluation.
 */
public enum Grade {
    S("Outstanding", 10.0, 90.0, 100.0),
    A("Excellent", 9.0, 80.0, 89.99),
    B("Very Good", 8.0, 70.0, 79.99),
    C("Good", 7.0, 60.0, 69.99),
    D("Satisfactory", 6.0, 50.0, 59.99),
    E("Pass", 5.0, 40.0, 49.99),
    F("Fail", 0.0, 0.0, 39.99),
    NOT_GRADED("In Progress", 0.0, -1.0, -1.0);

    private final String description;
    private final double gradePoint;
    private final double minScore;
    private final double maxScore;

    Grade(String description, double gradePoint, double minScore, double maxScore) {
        this.description = description;
        this.gradePoint = gradePoint;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public String getDescription() { return description; }
    public double getGradePoint() { return gradePoint; }
    public double getMinScore() { return minScore; }
    public double getMaxScore() { return maxScore; }

    public static Grade fromScore(double score) {
        if (score < 0.0) return NOT_GRADED;
        if (score >= 90.0) return S;
        if (score >= 80.0) return A;
        if (score >= 70.0) return B;
        if (score >= 60.0) return C;
        if (score >= 50.0) return D;
        if (score >= 40.0) return E;
        return F;
    }
}
