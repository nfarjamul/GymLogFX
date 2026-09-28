package com.gymlogfx.service;

import com.gymlogfx.model.WorkoutLog;

import java.util.List;

/**
 * VolumeAnalyzer - Concrete implementation of AbstractWorkoutAnalyzer.
 * Analyzes total training volume (sets × reps × weight).
 */
public class VolumeAnalyzer extends AbstractWorkoutAnalyzer {

    @Override
    protected String preprocess(List<WorkoutLog> logs) {
        return String.format("Analyzing %d workout sessions...", logs.size());
    }

    @Override
    protected double calculateMetric(List<WorkoutLog> logs) {
        return logs.stream()
            .mapToDouble(log -> log.getWeight() * log.getSets() * log.getReps())
            .sum();
    }

    @Override
    protected String formatResult(String preprocessed, double metric) {
        return String.format("%s\nTotal Volume: %.1f kg × reps", preprocessed, metric);
    }
}
