package com.gymlogfx.service;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.*;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * WorkoutService - Business logic layer.
 * Demonstrates the Service pattern separating business logic from data access.
 */
public class WorkoutService {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public boolean logWorkout(int userId, int exerciseId, String exerciseName,
                               String date, double weight, int sets, int reps, String notes) {
        return db.logWorkout(userId, exerciseId, exerciseName, date, weight, sets, reps, notes);
    }

    public ObservableList<WorkoutLog> getWorkoutLogs(int userId) {
        return db.getWorkoutLogs(userId);
    }

    public List<WorkoutLog> getProgressionData(int userId, String exerciseName) {
        return db.getProgressionData(userId, exerciseName);
    }

    public boolean updateWorkoutLog(int logId, double weight, int sets, int reps, String notes) {
        return db.updateWorkoutLog(logId, weight, sets, reps, notes);
    }

    public boolean deleteWorkoutLog(int logId) {
        return db.deleteWorkoutLog(logId);
    }

    public ObservableList<Exercise> getAllExercises() {
        return db.getAllExercises();
    }

    public int createRoutine(int userId, String name, String description) {
        return db.createRoutine(userId, name, description);
    }

    public ObservableList<Routine> getRoutines(int userId) {
        return db.getRoutines(userId);
    }

    public boolean updateRoutine(int id, String name, String desc) {
        return db.updateRoutine(id, name, desc);
    }

    public boolean deleteRoutine(int id) {
        return db.deleteRoutine(id);
    }

    public boolean addExerciseToRoutine(int routineId, int exerciseId, int sets, int reps, int order) {
        return db.addExerciseToRoutine(routineId, exerciseId, sets, reps, order);
    }

    public ObservableList<Exercise> getRoutineExercises(int routineId) {
        return db.getRoutineExercises(routineId);
    }

    /** Calculate 1RM estimate using Epley formula: weight * (1 + reps/30) */
    public double calculate1RM(double weight, int reps) {
        if (reps == 1) return weight;
        return weight * (1.0 + reps / 30.0);
    }
}
