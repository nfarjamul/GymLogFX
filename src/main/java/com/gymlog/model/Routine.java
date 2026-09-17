package com.gymlog.model;

import java.util.ArrayList;
import java.util.List;

/** A named, ordered collection of exercises the user wants to repeat as a session template. */
public class Routine {

    private String name;
    private final List<String> exercises;

    public Routine(String name) {
        this(name, new ArrayList<>());
    }

    public Routine(String name, List<String> exercises) {
        this.name = name;
        this.exercises = new ArrayList<>(exercises);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<String> getExercises() { return exercises; }

    public void addExercise(String exercise) {
        if (!exercises.contains(exercise)) {
            exercises.add(exercise);
        }
    }

    public void removeExercise(String exercise) {
        exercises.remove(exercise);
    }

    public String toCsvLine() {
        String exercisesJoined = String.join("|", exercises).replace(",", ";");
        return escape(name) + "," + exercisesJoined;
    }

    public static Routine fromCsvLine(String line) {
        int commaIdx = line.indexOf(',');
        if (commaIdx < 0) {
            return new Routine(unescape(line));
        }
        String name = unescape(line.substring(0, commaIdx));
        String rest = line.substring(commaIdx + 1);
        List<String> exercises = new ArrayList<>();
        if (!rest.isBlank()) {
            for (String ex : rest.split("\\|")) {
                if (!ex.isBlank()) exercises.add(ex);
            }
        }
        return new Routine(name, exercises);
    }

    private static String escape(String value) {
        return value.replace(",", ";");
    }

    private static String unescape(String value) {
        return value;
    }

    @Override
    public String toString() {
        return name + " (" + exercises.size() + " exercises)";
    }
}
