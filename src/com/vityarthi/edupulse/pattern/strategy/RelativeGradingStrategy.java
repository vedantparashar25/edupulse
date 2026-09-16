package com.vityarthi.edupulse.pattern.strategy;

import com.vityarthi.edupulse.common.Grade;
import java.util.List;

public class RelativeGradingStrategy implements GradingStrategy {
    @Override
    public String getStrategyName() {
        return "Relative Standard-Deviation (Bell-Curve) Grading";
    }

    @Override
    public Grade evaluateGrade(double score, List<Double> cohortScores) {
        if (cohortScores == null || cohortScores.size() < 3) {
            return Grade.fromScore(score);
        }

        double mean = cohortScores.stream().mapToDouble(Double::doubleValue).average().orElse(50.0);
        double variance = cohortScores.stream()
                .mapToDouble(s -> Math.pow(s - mean, 2))
                .average().orElse(1.0);
        double stdDev = Math.max(1.0, Math.sqrt(variance));

        if (score >= mean + (1.5 * stdDev)) return Grade.S;
        if (score >= mean + (1.0 * stdDev)) return Grade.A;
        if (score >= mean + (0.5 * stdDev)) return Grade.B;
        if (score >= mean) return Grade.C;
        if (score >= mean - (0.5 * stdDev)) return Grade.D;
        if (score >= mean - (1.0 * stdDev)) return Grade.E;
        return Grade.F;
    }
}
