package com.gymlogfx.service;

/**
 * Trackable - Interface demonstrating OOP Interface usage.
 * Any entity that can be tracked for progressive overload implements this.
 */
public interface Trackable {
    /** Get the unique identifier for this trackable item */
    int getId();

    /** Get the display name for this trackable item */
    String getName();

    /** Calculate the performance score/metric for this item */
    double calculateMetric();

    /** Get a human-readable summary of progress */
    default String getProgressSummary() {
        return String.format("%s - Metric: %.2f", getName(), calculateMetric());
    }
}
