package com.vityarthi.edupulse.pattern.strategy;

import com.vityarthi.edupulse.common.Grade;
import java.util.List;

public class AbsoluteGradingStrategy implements GradingStrategy {
    @Override
    public String getStrategyName() {
        return "Absolute Cutoff Grading";
    }

    @Override
    public Grade evaluateGrade(double score, List<Double> cohortScores) {
        return Grade.fromScore(score);
    }
}
