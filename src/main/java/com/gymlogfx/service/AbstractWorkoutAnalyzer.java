package com.gymlogfx.service;

import com.gymlogfx.model.WorkoutLog;

import java.util.List;

/**
 * AbstractWorkoutAnalyzer - Abstract class demonstrating OOP Abstract Class usage.
 * Provides template method pattern for different analysis strategies.
 */
public abstract class AbstractWorkoutAnalyzer {

    /**
     * Template method - defines the algorithm structure
     * Subclasses provide specific implementations
     */
    public final String analyze(List<WorkoutLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return "No workout data available for analysis.";
        }
        String preprocessed = preprocess(logs);
        double metric = calculateMetric(logs);
        return formatResult(preprocessed, metric);
    }

    /** Subclasses define how to preprocess the data */
    protected abstract String preprocess(List<WorkoutLog> logs);

    /** Subclasses define what metric to calculate */
    protected abstract double calculateMetric(List<WorkoutLog> logs);

    /** Subclasses define how to format the result */
    protected abstract String formatResult(String preprocessed, double metric);
}
