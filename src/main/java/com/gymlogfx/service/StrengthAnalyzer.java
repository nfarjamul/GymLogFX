package com.gymlogfx.service;

import com.gymlogfx.model.WorkoutLog;

import java.util.List;
import java.util.OptionalDouble;

/**
 * StrengthAnalyzer - Another concrete implementation of AbstractWorkoutAnalyzer.
 * Analyzes peak strength (maximum weight lifted).
 */
public class StrengthAnalyzer extends AbstractWorkoutAnalyzer {

    @Override
    protected String preprocess(List<WorkoutLog> logs) {
        return String.format("Scanning %d entries for max strength...", logs.size());
    }

    @Override
    protected double calculateMetric(List<WorkoutLog> logs) {
        OptionalDouble max = logs.stream()
            .mapToDouble(WorkoutLog::getWeight)
            .max();
        return max.orElse(0.0);
    }

    @Override
    protected String formatResult(String preprocessed, double metric) {
        return String.format("%s\nPeak Weight: %.1f kg", preprocessed, metric);
    }
}
