package com.gymlog.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * A single logged unit of work: one exercise, on one date, at a given
 * weight, for a given number of reps and sets.
 */
public class WorkoutEntry {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final String id;
    private LocalDate date;
    private String exerciseName;
    private double weight;
    private int reps;
    private int sets;
    private String notes;

    public WorkoutEntry(LocalDate date, String exerciseName, double weight, int reps, int sets, String notes) {
        this(UUID.randomUUID().toString(), date, exerciseName, weight, reps, sets, notes);
    }

    public WorkoutEntry(String id, LocalDate date, String exerciseName, double weight, int reps, int sets, String notes) {
        this.id = id;
        this.date = date;
        this.exerciseName = exerciseName;
        this.weight = weight;
        this.reps = reps;
        this.sets = sets;
        this.notes = notes == null ? "" : notes;
    }

    public String getId() { return id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getExerciseName() { return exerciseName; }
    public void setExerciseName(String exerciseName) { this.exerciseName = exerciseName; }
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    public int getReps() { return reps; }
    public void setReps(int reps) { this.reps = reps; }
    public int getSets() { return sets; }
    public void setSets(int sets) { this.sets = sets; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    /** Simple total-volume metric (weight x reps x sets), used as a secondary overload signal. */
    public double getVolume() {
        return weight * reps * sets;
    }

    public String toCsvLine() {
        return String.join(",",
                escape(id),
                date.format(DATE_FORMAT),
                escape(exerciseName),
                String.valueOf(weight),
                String.valueOf(reps),
                String.valueOf(sets),
                escape(notes));
    }

    public static WorkoutEntry fromCsvLine(String line) {
        String[] parts = splitCsv(line);
        return new WorkoutEntry(
                parts[0],
                LocalDate.parse(parts[1], DATE_FORMAT),
                unescape(parts[2]),
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]),
                Integer.parseInt(parts[5]),
                parts.length > 6 ? unescape(parts[6]) : "");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace(",", "\\,");
    }

    private static String unescape(String value) {
        return value.replace("\\,", ",").replace("\\\\", "\\");
    }

    /** Splits on commas that are not preceded by a backslash (see escape()). */
    private static String[] splitCsv(String line) {
        return line.split("(?<!\\\\),");
    }

    @Override
    public String toString() {
        return date + "  " + exerciseName + "  " + weight + "kg x " + reps + " reps x " + sets + " sets";
    }
}
