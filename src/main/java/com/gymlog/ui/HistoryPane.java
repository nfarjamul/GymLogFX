package com.gymlog.ui;

import com.gymlog.concurrency.StatsCalculator;
import com.gymlog.concurrency.StatsCalculator.ProgressPoint;
import com.gymlog.concurrency.StatsCalculator.ProgressResult;
import com.gymlog.data.WorkoutRepository;
import com.gymlog.model.WorkoutEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * "History &amp; Charts" tab: pick an exercise, see a LineChart of its top
 * set weight over time (progressive-overload tracking).
 *
 * <p>Every recalculation is offloaded to {@link StatsCalculator}'s
 * ExecutorService so the JavaFX Application Thread is never blocked, even
 * if the workout history grows large. A short-lived helper {@link Thread}
 * blocks on the returned {@link Future} and hands the result back to the
 * UI thread via {@code Platform.runLater}, which is the only safe way to
 * touch live JavaFX nodes from a non-UI thread.
 */
public class HistoryPane {

    private final WorkoutRepository repository;
    private final StatsCalculator statsCalculator;

    private final BorderPane view = new BorderPane();
    private final ComboBox<String> exerciseSelector = new ComboBox<>();
    private final LineChart<String, Number> chart;
    private final Label summaryLabel = new Label();
    private final Label loadingLabel = new Label();

    public HistoryPane(WorkoutRepository repository, StatsCalculator statsCalculator) {
        this.repository = repository;
        this.statsCalculator = statsCalculator;

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Top Set Weight (kg)");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Progressive Overload");
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        build();
        refreshExerciseList();
    }

    public Parent getView() {
        return view;
    }

    private void build() {
        exerciseSelector.setPromptText("Select an exercise");
        exerciseSelector.setOnAction(e -> loadChartAsync());

        Button refreshButton = new Button("Recalculate");
        refreshButton.setTooltip(new Tooltip("Recompute the chart in the background (ExecutorService)"));
        refreshButton.setOnAction(e -> loadChartAsync());

        loadingLabel.getStyleClass().add("loading-label");

        HBox controls = new HBox(10, new Label("Exercise:"), exerciseSelector, refreshButton, loadingLabel);
        controls.setPadding(new Insets(15));

        summaryLabel.getStyleClass().add("summary-label");

        VBox top = new VBox(controls, summaryLabel);
        view.setTop(top);
        view.setCenter(chart);
    }

    /** Rebuilds the exercise dropdown from the repository's known exercise names, then reloads the chart. */
    public void refreshExerciseList() {
        String previouslySelected = exerciseSelector.getValue();
        List<String> names = repository.getKnownExerciseNames();
        exerciseSelector.setItems(FXCollections.observableArrayList(names));
        if (previouslySelected != null && names.contains(previouslySelected)) {
            exerciseSelector.setValue(previouslySelected);
        } else if (!names.isEmpty()) {
            exerciseSelector.setValue(names.get(0));
        }
        loadChartAsync();
    }

    private void loadChartAsync() {
        String exercise = exerciseSelector.getValue();
        if (exercise == null) {
            chart.getData().clear();
            summaryLabel.setText("Log some sets to see progress here.");
            return;
        }

        loadingLabel.setText("Calculating...");
        List<WorkoutEntry> entriesForExercise = repository.getEntriesForExercise(exercise);
        Future<ProgressResult> future = statsCalculator.computeProgressionAsync(entriesForExercise);

        Thread waiter = new Thread(() -> {
            try {
                ProgressResult result = future.get();
                Platform.runLater(() -> applyResult(exercise, result));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                Platform.runLater(() -> {
                    loadingLabel.setText("");
                    summaryLabel.setText("Failed to compute progress: " + e.getCause());
                });
            }
        }, "Stats-Waiter");
        waiter.setDaemon(true);
        waiter.start();
    }

    private void applyResult(String exercise, ProgressResult result) {
        loadingLabel.setText("");
        chart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(exercise);
        for (ProgressPoint point : result.points) {
            series.getData().add(new XYChart.Data<>(StatsCalculator.formatLabel(point.date()), point.topWeight()));
        }
        chart.getData().add(series);

        if (result.points.isEmpty()) {
            summaryLabel.setText("No sessions logged for " + exercise + " yet.");
        } else {
            String trend = result.percentChange >= 0 ? "up" : "down";
            summaryLabel.setText(String.format(
                    "%s: %.1f kg \u2192 %.1f kg across %d session(s) (%s %.1f%%)",
                    exercise, result.startWeight, result.latestWeight, result.points.size(),
                    trend, Math.abs(result.percentChange)));
        }
    }
}
