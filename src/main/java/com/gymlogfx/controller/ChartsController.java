package com.gymlogfx.controller;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.Exercise;
import com.gymlogfx.model.WorkoutLog;
import com.gymlogfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ChartsController {
    @FXML public ComboBox<Exercise> exerciseSelector;
    @FXML public PieChart muscleGroupChart;
    @FXML public BarChart<String, Number> volumeChart;
    @FXML public LineChart<String, Number> weightChart;

    private Map<Integer, String> exerciseMuscleMap;

    @FXML
    public void initialize() {
        // Build map for muscle group lookups
        exerciseMuscleMap = DatabaseManager.getInstance().getAllExercises().stream()
                .collect(Collectors.toMap(Exercise::getId, Exercise::getMuscleGroup));

        // Setup exercise combo box
        ObservableList<Exercise> exercises = DatabaseManager.getInstance().getAllExercises();
        exerciseSelector.setItems(exercises);

        populateMuscleGroupChart();

        // Pre-select first exercise and populate charts if available
        if (!exercises.isEmpty()) {
            exerciseSelector.getSelectionModel().select(0);
            updateCharts(null);
        }
    }

    private void populateMuscleGroupChart() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        // Get all logs for the user to determine muscle group distribution
        ObservableList<WorkoutLog> allLogs = DatabaseManager.getInstance().getWorkoutLogs(userId);

        Map<String, Integer> muscleCounts = new HashMap<>();
        for (WorkoutLog log : allLogs) {
            String muscle = exerciseMuscleMap.getOrDefault(log.getExerciseId(), "Unknown");
            muscleCounts.put(muscle, muscleCounts.getOrDefault(muscle, 0) + 1);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : muscleCounts.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
        }

        muscleGroupChart.setData(pieChartData);
    }

    @FXML 
    public void updateCharts(ActionEvent e) {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        
        Exercise selected = exerciseSelector.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        int userId = SessionManager.getInstance().getCurrentUser().getId();
        
        // Fetch specific progression data for this exercise
        List<WorkoutLog> progressionData = DatabaseManager.getInstance().getProgressionData(userId, selected.getName());

        // Clear existing data
        weightChart.getData().clear();
        volumeChart.getData().clear();

        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName(selected.getName() + " - Max Weight");

        XYChart.Series<String, Number> volumeSeries = new XYChart.Series<>();
        volumeSeries.setName(selected.getName() + " - Total Volume");

        // Group by date to find max weight and sum volume per date (TreeMap ensures chronological sorting)
        Map<String, Double> maxWeightPerDate = new TreeMap<>();
        Map<String, Double> totalVolumePerDate = new TreeMap<>();

        for (WorkoutLog log : progressionData) {
            String date = log.getWorkoutDate();
            double weight = log.getWeight();
            double volume = log.getTotalVolume();

            maxWeightPerDate.put(date, Math.max(maxWeightPerDate.getOrDefault(date, 0.0), weight));
            totalVolumePerDate.put(date, totalVolumePerDate.getOrDefault(date, 0.0) + volume);
        }

        // Add data points to series
        for (String date : maxWeightPerDate.keySet()) {
            weightSeries.getData().add(new XYChart.Data<>(date, maxWeightPerDate.get(date)));
            volumeSeries.getData().add(new XYChart.Data<>(date, totalVolumePerDate.get(date)));
        }

        weightChart.getData().add(weightSeries);
        volumeChart.getData().add(volumeSeries);
    }
}
