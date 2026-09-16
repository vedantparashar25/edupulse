package com.vityarthi.edupulse.pattern.strategy;

import com.vityarthi.edupulse.common.Grade;
import java.util.List;

public interface GradingStrategy {
    String getStrategyName();
    Grade evaluateGrade(double score, List<Double> cohortScores);
}
