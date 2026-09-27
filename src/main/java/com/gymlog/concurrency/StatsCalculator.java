package com.gymlog.concurrency;

import com.gymlog.model.WorkoutEntry;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Wraps a {@link java.util.concurrent.ExecutorService} so the rest of the
 * app never has to manage raw threads for background computation. This is
 * the "introduction to the Java Concurrency Package" piece of the app:
 * instead of writing {@code new Thread(...).start()} every time we need
 * background work done, we submit {@link Callable} tasks to a small,
 * reusable, named thread pool and get a {@link Future} back.
 *
 * <p>Used by the History tab to turn a (potentially large) list of raw
 * {@link WorkoutEntry} objects into per-date progressive-overload points
 * without blocking the JavaFX Application Thread.
 */
public class StatsCalculator {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    // A small fixed pool is enough for this app's background workload;
    // a custom ThreadFactory gives the worker threads readable names and
    // marks them daemon so they never prevent JVM shutdown.
    private final ExecutorService executor = Executors.newFixedThreadPool(2, runnable -> {
        Thread t = new Thread(runnable, "Stats-Worker");
        t.setDaemon(true);
        return t;
    });

    /** One point of progressive-overload data: a date, its heaviest set, and total volume that day. */
    public record ProgressPoint(LocalDate date, double topWeight, double bestVolume) { }

    /** The aggregated result of a progression computation for one exercise. */
    public static class ProgressResult {
        public final List<ProgressPoint> points;
        public final double startWeight;
        public final double latestWeight;
        public final double percentChange;

        ProgressResult(List<ProgressPoint> points) {
            this.points = points;
            if (points.isEmpty()) {
                startWeight = 0;
                latestWeight = 0;
                percentChange = 0;
            } else {
                startWeight = points.get(0).topWeight();
                latestWeight = points.get(points.size() - 1).topWeight();
                percentChange = startWeight == 0 ? 0 : ((latestWeight - startWeight) / startWeight) * 100.0;
            }
        }
    }

    /**
     * Submits a background task that reduces raw entries into one
     * "top weight" point per date, sorted chronologically. Returns
     * immediately with a {@link Future}; the caller decides how to wait
     * for the result (see {@code HistoryPane}, which waits on a helper
     * thread and marshals the outcome back with {@code Platform.runLater}).
     */
    public Future<ProgressResult> computeProgressionAsync(List<WorkoutEntry> entriesForExercise) {
        Callable<ProgressResult> task = () -> {
            Map<LocalDate, ProgressPoint> byDate = new TreeMap<>();
            for (WorkoutEntry e : entriesForExercise) {
                ProgressPoint existing = byDate.get(e.getDate());
                double topWeight = existing == null ? e.getWeight() : Math.max(existing.topWeight(), e.getWeight());
                double volume = (existing == null ? 0 : existing.bestVolume()) + e.getVolume();
                byDate.put(e.getDate(), new ProgressPoint(e.getDate(), topWeight, volume));
            }
            return new ProgressResult(new ArrayList<>(byDate.values()));
        };
        return executor.submit(task);
    }

    public static String formatLabel(LocalDate date) {
        return date.format(LABEL_FORMAT);
    }

    /** Shuts the pool down gracefully; call exactly once, when the application exits. */
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
