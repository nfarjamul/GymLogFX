package com.gymlogfx.model;

import javafx.beans.property.*;

/**
 * WorkoutLog - records a specific set performed during a workout session
 * Demonstrates progressive overload tracking
 */
public class WorkoutLog {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final IntegerProperty exerciseId = new SimpleIntegerProperty();
    private final StringProperty exerciseName = new SimpleStringProperty();
    private final StringProperty workoutDate = new SimpleStringProperty();
    private final DoubleProperty weight = new SimpleDoubleProperty();
    private final IntegerProperty sets = new SimpleIntegerProperty();
    private final IntegerProperty reps = new SimpleIntegerProperty();
    private final StringProperty notes = new SimpleStringProperty();

    public WorkoutLog() {}

    public WorkoutLog(int id, int userId, int exerciseId, String exerciseName,
                      String workoutDate, double weight, int sets, int reps, String notes) {
        setId(id);
        setUserId(userId);
        setExerciseId(exerciseId);
        setExerciseName(exerciseName);
        setWorkoutDate(workoutDate);
        setWeight(weight);
        setSets(sets);
        setReps(reps);
        setNotes(notes);
    }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public IntegerProperty userIdProperty() { return userId; }
    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public IntegerProperty exerciseIdProperty() { return exerciseId; }
    public int getExerciseId() { return exerciseId.get(); }
    public void setExerciseId(int v) { exerciseId.set(v); }

    public StringProperty exerciseNameProperty() { return exerciseName; }
    public String getExerciseName() { return exerciseName.get(); }
    public void setExerciseName(String v) { exerciseName.set(v); }

    public StringProperty workoutDateProperty() { return workoutDate; }
    public String getWorkoutDate() { return workoutDate.get(); }
    public void setWorkoutDate(String v) { workoutDate.set(v); }

    public DoubleProperty weightProperty() { return weight; }
    public double getWeight() { return weight.get(); }
    public void setWeight(double v) { weight.set(v); }

    public IntegerProperty setsProperty() { return sets; }
    public int getSets() { return sets.get(); }
    public void setSets(int v) { sets.set(v); }

    public IntegerProperty repsProperty() { return reps; }
    public int getReps() { return reps.get(); }
    public void setReps(int v) { reps.set(v); }

    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v); }

    /** Calculates total volume for progressive overload tracking */
    public double getTotalVolume() {
        return getWeight() * getSets() * getReps();
    }
}
