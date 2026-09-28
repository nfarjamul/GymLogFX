package com.gymlogfx.controller;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.WorkoutLog;
import com.gymlogfx.service.ApiService;
import com.gymlogfx.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {
    @FXML public Label welcomeLabel, dateLabel, totalWorkoutsLabel, thisWeekLabel, bestLiftLabel, totalVolumeLabel, apiStatusLabel;
    @FXML public ProgressIndicator quoteLoadingIndicator;
    @FXML public ListView<String> quotesList;
    @FXML public TableView<WorkoutLog> recentWorkoutsTable;
    @FXML public TableColumn<WorkoutLog, String> colDate, colExercise;
    @FXML public TableColumn<WorkoutLog, Integer> colSets, colReps;
    @FXML public TableColumn<WorkoutLog, Double> colVolume, colWeight;

    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        // Setup Date Label
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        
        // Setup user welcome
        if (SessionManager.getInstance().getCurrentUser() != null) {
            welcomeLabel.setText("Good Morning, " + SessionManager.getInstance().getCurrentUser().getUsername() + "!");
        }

        // Setup table columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("workoutDate"));
        colExercise.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colReps.setCellValueFactory(new PropertyValueFactory<>("reps"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("totalVolume"));

        // Load data
        loadDashboardData();
        refreshQuotes(null);
    }

    private void loadDashboardData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        ObservableList<WorkoutLog> logs = DatabaseManager.getInstance().getWorkoutLogs(userId);

        // Bind data to table
        recentWorkoutsTable.setItems(logs);
        
        // Calculate stats
        totalWorkoutsLabel.setText(String.valueOf(logs.size()));

        double maxWeight = 0;
        double totalVolume = 0;
        int thisWeek = 0;
        
        LocalDate oneWeekAgo = LocalDate.now().minusDays(7);

        for (WorkoutLog log : logs) {
            maxWeight = Math.max(maxWeight, log.getWeight());
            totalVolume += log.getTotalVolume();
            
            try {
                LocalDate date = LocalDate.parse(log.getWorkoutDate());
                if (date.isAfter(oneWeekAgo) || date.isEqual(oneWeekAgo)) {
                    thisWeek++;
                }
            } catch (Exception ignored) {}
        }

        bestLiftLabel.setText(String.format("%.1f kg", maxWeight));
        totalVolumeLabel.setText(String.format("%.1f kg", totalVolume));
        thisWeekLabel.setText(String.valueOf(thisWeek));
    }

    @FXML 
    public void refreshQuotes(ActionEvent e) {
        quoteLoadingIndicator.setVisible(true);
        apiStatusLabel.setVisible(true);
        quotesList.getItems().clear();

        Task<List<String>> task = apiService.createQuoteFetchTask();
        
        // Bind the status label to the background task's progress message
        apiStatusLabel.textProperty().bind(task.messageProperty());
        
        task.setOnSucceeded(event -> {
            quotesList.getItems().addAll(task.getValue());
            quoteLoadingIndicator.setVisible(false);
            apiStatusLabel.textProperty().unbind();
            apiStatusLabel.setText("Loaded from API");
        });
        
        task.setOnFailed(event -> {
            quoteLoadingIndicator.setVisible(false);
            apiStatusLabel.textProperty().unbind();
            apiStatusLabel.setText("Failed to load quotes");
        });

        // Run task on a background thread
        new Thread(task).start();
    }
}
