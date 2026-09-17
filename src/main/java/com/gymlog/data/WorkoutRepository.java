package com.gymlog.data;

import com.gymlog.model.Routine;
import com.gymlog.model.WorkoutEntry;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Central, in-memory store for workout entries and routines.
 *
 * <p>This object is a <b>shared resource</b>: the JavaFX Application Thread
 * reads and writes it in direct response to user actions, a background
 * {@code AutoSave-Thread} periodically reads the whole state to persist it
 * to disk, and {@code Stats-Worker} threads (from the executor behind
 * {@code StatsCalculator}) read entries to build chart data. Without
 * coordination, one thread could be iterating the entries list while
 * another mutates it, causing a {@code ConcurrentModificationException} or
 * a corrupted save file.
 *
 * <p>To keep this safe, every method that touches {@link #entries} or
 * {@link #routines} is {@code synchronized} on this object's intrinsic
 * lock. Read methods return a defensive copy so callers can iterate the
 * result on any thread without holding the lock. This is intentionally the
 * simplest possible correct approach (a single monitor guarding all
 * shared state) rather than fine-grained locking, which would be overkill
 * for a desktop app of this size.
 */
public class WorkoutRepository {

    private static final Path DATA_DIR = Paths.get(System.getProperty("user.home"), ".gymlogfx");
    private static final Path ENTRIES_FILE = DATA_DIR.resolve("workouts.csv");
    private static final Path ROUTINES_FILE = DATA_DIR.resolve("routines.csv");

    private static final String[] DEFAULT_EXERCISES = {
            "Bench Press", "Back Squat", "Deadlift", "Overhead Press",
            "Barbell Row", "Pull-Up", "Bicep Curl", "Lat Pulldown"
    };

    private final List<WorkoutEntry> entries = new ArrayList<>();
    private final List<Routine> routines = new ArrayList<>();

    /** Returns a snapshot copy that is safe to iterate on any thread. */
    public synchronized List<WorkoutEntry> getAllEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized void addEntry(WorkoutEntry entry) {
        entries.add(entry);
    }

    public synchronized void removeEntry(WorkoutEntry entry) {
        entries.remove(entry);
    }

    public synchronized List<WorkoutEntry> getEntriesForExercise(String exerciseName) {
        List<WorkoutEntry> result = new ArrayList<>();
        for (WorkoutEntry e : entries) {
            if (e.getExerciseName().equalsIgnoreCase(exerciseName)) {
                result.add(e);
            }
        }
        return result;
    }

    /** Distinct exercise names: the built-in defaults plus anything the user has logged. */
    public synchronized List<String> getKnownExerciseNames() {
        Set<String> names = new LinkedHashSet<>();
        for (String d : DEFAULT_EXERCISES) names.add(d);
        for (WorkoutEntry e : entries) names.add(e.getExerciseName());
        return new ArrayList<>(names);
    }

    public synchronized List<Routine> getRoutines() {
        return new ArrayList<>(routines);
    }

    public synchronized void addRoutine(Routine routine) {
        routines.add(routine);
    }

    public synchronized void removeRoutine(Routine routine) {
        routines.remove(routine);
    }

    /**
     * Persists the current in-memory state to disk. Safe to call from any
     * thread (the UI thread on shutdown, or the AutoSave-Thread
     * periodically); synchronized so the lists cannot be mutated by
     * another thread while this method is iterating them.
     */
    public synchronized void save() {
        try {
            Files.createDirectories(DATA_DIR);
            try (BufferedWriter writer = Files.newBufferedWriter(ENTRIES_FILE, StandardCharsets.UTF_8)) {
                for (WorkoutEntry e : entries) {
                    writer.write(e.toCsvLine());
                    writer.newLine();
                }
            }
            try (BufferedWriter writer = Files.newBufferedWriter(ROUTINES_FILE, StandardCharsets.UTF_8)) {
                for (Routine r : routines) {
                    writer.write(r.toCsvLine());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("[WorkoutRepository] Failed to save data: " + e.getMessage());
        }
    }

    /** Loads persisted state from disk into memory. Intended to be called once, at startup. */
    public synchronized void load() {
        entries.clear();
        routines.clear();

        if (Files.exists(ENTRIES_FILE)) {
            try (BufferedReader reader = Files.newBufferedReader(ENTRIES_FILE, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    try {
                        entries.add(WorkoutEntry.fromCsvLine(line));
                    } catch (Exception parseError) {
                        System.err.println("[WorkoutRepository] Skipping malformed entry line: " + line);
                    }
                }
            } catch (IOException e) {
                System.err.println("[WorkoutRepository] Failed to load entries: " + e.getMessage());
            }
        }

        if (Files.exists(ROUTINES_FILE)) {
            try (BufferedReader reader = Files.newBufferedReader(ROUTINES_FILE, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) continue;
                    routines.add(Routine.fromCsvLine(line));
                }
            } catch (IOException e) {
                System.err.println("[WorkoutRepository] Failed to load routines: " + e.getMessage());
            }
        }
    }
}
