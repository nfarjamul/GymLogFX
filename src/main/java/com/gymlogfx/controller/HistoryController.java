package com.gymlogfx.controller;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.Exercise;
import com.gymlogfx.model.WorkoutLog;
import com.gymlogfx.util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.stream.Collectors;

public class HistoryController {
    @FXML public TableView<WorkoutLog> historyTable;
    @FXML public TableColumn<WorkoutLog, String> colDate, colExercise, colMuscle, colNotes;
    @FXML public TableColumn<WorkoutLog, Integer> colReps, colSets;
    @FXML public TableColumn<WorkoutLog, Double> colVolume, colWeight;
    @FXML public TextField searchField;
    @FXML public Label totalEntriesLabel;

    private Map<Integer, String> exerciseMuscleMap;
    private ObservableList<WorkoutLog> allLogs;

    @FXML
    public void initialize() {
        // Build map for quick muscle group lookups (WorkoutLog only has exerciseId)
        exerciseMuscleMap = DatabaseManager.getInstance().getAllExercises().stream()
                .collect(Collectors.toMap(Exercise::getId, Exercise::getMuscleGroup));

        // Setup Columns
        colDate.setCellValueFactory(new PropertyValueFactory<>("workoutDate"));
        colExercise.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colReps.setCellValueFactory(new PropertyValueFactory<>("reps"));
        colVolume.setCellValueFactory(new PropertyValueFactory<>("totalVolume"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Custom CellFactory for Muscle Group
        colMuscle.setCellValueFactory(cellData -> {
            int exId = cellData.getValue().getExerciseId();
            String muscle = exerciseMuscleMap.getOrDefault(exId, "Unknown");
            return new SimpleStringProperty(muscle);
        });

        loadHistoryData();
    }

    private void loadHistoryData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        
        allLogs = DatabaseManager.getInstance().getWorkoutLogs(userId);
        historyTable.setItems(allLogs);
        totalEntriesLabel.setText("Total entries: " + allLogs.size());
    }

    @FXML 
    public void handleSearch(ActionEvent e) {
        String query = searchField.getText().toLowerCase();
        if (query.isEmpty()) {
            showAll(null);
            return;
        }

        ObservableList<WorkoutLog> filtered = FXCollections.observableArrayList(
            allLogs.stream().filter(log -> 
                log.getExerciseName().toLowerCase().contains(query) ||
                exerciseMuscleMap.getOrDefault(log.getExerciseId(), "").toLowerCase().contains(query) ||
                (log.getNotes() != null && log.getNotes().toLowerCase().contains(query))
            ).collect(Collectors.toList())
        );
        historyTable.setItems(filtered);
        totalEntriesLabel.setText("Total entries: " + filtered.size());
    }

    @FXML 
    public void showAll(ActionEvent e) {
        searchField.clear();
        historyTable.setItems(allLogs);
        totalEntriesLabel.setText("Total entries: " + allLogs.size());
    }

    @FXML 
    public void deleteSelected(ActionEvent e) {
        WorkoutLog selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean success = DatabaseManager.getInstance().deleteWorkoutLog(selected.getId());
            if (success) {
                loadHistoryData();
                // Optionally clear search filter if they just deleted
                searchField.clear();
            }
        }
    }

    @FXML 
    public void exportCsv(ActionEvent e) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Workout History");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("workout_history.csv");
        
        File file = fileChooser.showSaveDialog(historyTable.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // Write Header
                writer.println("Date,Exercise,Muscle Group,Weight (kg),Sets,Reps,Volume (kg),Notes");
                
                // Write Rows (based on currently visible/filtered items)
                for (WorkoutLog log : historyTable.getItems()) {
                    String muscle = exerciseMuscleMap.getOrDefault(log.getExerciseId(), "Unknown");
                    String notes = log.getNotes() == null ? "" : log.getNotes().replace(",", ";").replace("\n", " ");
                    
                    writer.printf("%s,%s,%s,%.1f,%d,%d,%.1f,%s%n",
                            log.getWorkoutDate(),
                            log.getExerciseName(),
                            muscle,
                            log.getWeight(),
                            log.getSets(),
                            log.getReps(),
                            log.getTotalVolume(),
                            notes
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
