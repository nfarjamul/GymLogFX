package com.gymlogfx.controller;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.Exercise;
import com.gymlogfx.model.WorkoutLog;
import com.gymlogfx.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class WorkoutLogController {
    @FXML public TableColumn<WorkoutLog, String> colDate, colExercise, colNotes;
    @FXML public TableColumn<WorkoutLog, Integer> colReps, colSets;
    @FXML public TableColumn<WorkoutLog, Double> colWeight;
    @FXML public ComboBox<Exercise> exerciseCombo;
    @FXML public TextArea notesArea;
    @FXML public Label oneRmLabel, statusLabel;
    @FXML public Spinner<Integer> repsSpinner, setsSpinner;
    @FXML public TextField weightField;
    @FXML public DatePicker workoutDate;
    @FXML public TableView<WorkoutLog> workoutTable;

    private Integer editingLogId = null;

    @FXML
    public void initialize() {
        // Initialize ComboBox
        exerciseCombo.setItems(DatabaseManager.getInstance().getAllExercises());

        // Default Date
        workoutDate.setValue(LocalDate.now());

        // Setup Table
        colDate.setCellValueFactory(new PropertyValueFactory<>("workoutDate"));
        colExercise.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colSets.setCellValueFactory(new PropertyValueFactory<>("sets"));
        colReps.setCellValueFactory(new PropertyValueFactory<>("reps"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Load data
        loadWorkoutData();

        // Listeners for UI updates
        weightField.textProperty().addListener((obs, oldV, newV) -> calculate1RM());
        repsSpinner.valueProperty().addListener((obs, oldV, newV) -> calculate1RM());
        workoutDate.valueProperty().addListener((obs, oldV, newV) -> loadWorkoutData());
    }

    private void calculate1RM() {
        try {
            double weight = Double.parseDouble(weightField.getText());
            int reps = repsSpinner.getValue();
            if (reps == 1) {
                oneRmLabel.setText(String.format("Estimated 1RM: %.1f kg", weight));
            } else if (reps > 1) {
                // Epley formula
                double oneRm = weight * (1.0 + (reps / 30.0));
                oneRmLabel.setText(String.format("Estimated 1RM: %.1f kg", oneRm));
            }
        } catch (NumberFormatException e) {
            oneRmLabel.setText("Estimated 1RM: -");
        }
    }

    private void loadWorkoutData() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        
        ObservableList<WorkoutLog> logs = DatabaseManager.getInstance().getWorkoutLogs(userId);
        
        LocalDate selectedDate = workoutDate.getValue();
        if (selectedDate != null) {
            String dateStr = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            ObservableList<WorkoutLog> filtered = FXCollections.observableArrayList(
                logs.stream().filter(l -> l.getWorkoutDate().equals(dateStr)).collect(Collectors.toList())
            );
            workoutTable.setItems(filtered);
        } else {
            workoutTable.setItems(logs);
        }
    }

    @FXML 
    public void clearForm(ActionEvent e) {
        editingLogId = null;
        exerciseCombo.getSelectionModel().clearSelection();
        weightField.setText("");
        notesArea.setText("");
        workoutDate.setValue(LocalDate.now());
        statusLabel.setText("");
    }

    @FXML 
    public void handleDelete(ActionEvent e) {
        WorkoutLog selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DatabaseManager.getInstance().deleteWorkoutLog(selected.getId());
            loadWorkoutData();
            statusLabel.setText("Workout deleted.");
        } else {
            statusLabel.setText("Select a workout to delete.");
        }
    }

    @FXML 
    public void handleEdit(ActionEvent e) {
        WorkoutLog selected = workoutTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editingLogId = selected.getId();
            
            // Select exercise in combo
            for (Exercise ex : exerciseCombo.getItems()) {
                if (ex.getId() == selected.getExerciseId()) {
                    exerciseCombo.getSelectionModel().select(ex);
                    break;
                }
            }
            
            workoutDate.setValue(LocalDate.parse(selected.getWorkoutDate()));
            weightField.setText(String.valueOf(selected.getWeight()));
            setsSpinner.getValueFactory().setValue(selected.getSets());
            repsSpinner.getValueFactory().setValue(selected.getReps());
            notesArea.setText(selected.getNotes() == null ? "" : selected.getNotes());
            
            statusLabel.setText("Editing log...");
        }
    }

    @FXML 
    public void handleLogWorkout(ActionEvent e) {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        
        Exercise exercise = exerciseCombo.getSelectionModel().getSelectedItem();
        if (exercise == null) {
            statusLabel.setText("Please select an exercise.");
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightField.getText());
        } catch (NumberFormatException ex) {
            statusLabel.setText("Invalid weight format.");
            return;
        }

        LocalDate date = workoutDate.getValue();
        if (date == null) {
            statusLabel.setText("Please select a date.");
            return;
        }

        int sets = setsSpinner.getValue();
        int reps = repsSpinner.getValue();
        String notes = notesArea.getText();
        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        int userId = SessionManager.getInstance().getCurrentUser().getId();

        boolean success;
        if (editingLogId != null) {
            success = DatabaseManager.getInstance().updateWorkoutLog(editingLogId, weight, sets, reps, notes);
        } else {
            success = DatabaseManager.getInstance().logWorkout(userId, exercise.getId(), exercise.getName(), dateStr, weight, sets, reps, notes);
        }

        if (success) {
            statusLabel.setText(editingLogId != null ? "Workout updated!" : "Workout logged successfully!");
            editingLogId = null;
            loadWorkoutData();
            weightField.setText("");
            notesArea.setText("");
            exerciseCombo.getSelectionModel().clearSelection();
        } else {
            statusLabel.setText("Database error.");
        }
    }
}
