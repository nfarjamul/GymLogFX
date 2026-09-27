package com.gymlog.concurrency;

import com.gymlog.data.WorkoutRepository;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * A plain {@link Runnable} meant to be run on its own {@link Thread}
 * (see {@code Main.start()}).
 *
 * <p>This class demonstrates the classic Thread/Runnable lifecycle used
 * throughout the "Thread and Runnable" and "thread lifecycle" topics:
 * <ul>
 *   <li><b>New</b> - the worker object is constructed, and wrapped in a
 *       {@code new Thread(worker, "AutoSave-Thread")}.</li>
 *   <li><b>Runnable/Running</b> - {@code Thread.start()} invokes
 *       {@link #run()} on the new thread, which loops, sleeping between
 *       autosaves.</li>
 *   <li><b>Timed Waiting</b> - the thread spends most of its life parked
 *       in {@code Thread.sleep(...)} between saves.</li>
 *   <li><b>Terminated</b> - {@link #stop()} flips a volatile flag and the
 *       owning code calls {@code Thread.interrupt()} to wake the thread
 *       from sleep immediately; the loop then exits and {@code run()}
 *       returns, after which {@code Thread.join()} in Main can safely
 *       wait for the thread to fully terminate.</li>
 * </ul>
 */
public class AutoSaveWorker implements Runnable {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final long INTERVAL_MILLIS = 15_000; // autosave every 15 seconds

    private final WorkoutRepository repository;
    private final Consumer<String> statusCallback;

    // volatile: the UI thread calls stop() while this thread reads
    // "running" in its loop condition, on two different CPU caches.
    private volatile boolean running = true;

    public AutoSaveWorker(WorkoutRepository repository, Consumer<String> statusCallback) {
        this.repository = repository;
        this.statusCallback = statusCallback;
    }

    @Override
    public void run() {
        statusCallback.accept("Autosave thread started");
        while (running) {
            try {
                Thread.sleep(INTERVAL_MILLIS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupted status
                break; // woken up for shutdown; exit the loop
            }
            if (!running) break;

            repository.save(); // synchronized on the shared repository
            statusCallback.accept("Autosaved at " + LocalTime.now().format(TIME_FORMAT));
        }
        statusCallback.accept("Autosave thread stopped");
    }

    /** Cooperative stop signal. Pair with {@code Thread.interrupt()} to wake the thread from sleep(). */
    public void stop() {
        running = false;
    }
}
