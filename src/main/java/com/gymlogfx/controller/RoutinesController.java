package com.gymlogfx.controller;

import com.gymlogfx.database.DatabaseManager;
import com.gymlogfx.model.Exercise;
import com.gymlogfx.model.Routine;
import com.gymlogfx.util.SessionManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class RoutinesController {
    @FXML public ComboBox<Exercise> addExerciseCombo;
    @FXML public Label routineDescLabel, routineNameLabel;
    @FXML public ListView<Exercise> routineExercisesList;
    @FXML public ListView<Routine> routinesList;

    @FXML
    public void initialize() {
        if (SessionManager.getInstance().getCurrentUser() == null) return;
        
        addExerciseCombo.setItems(DatabaseManager.getInstance().getAllExercises());
        loadRoutines();
    }

    private void loadRoutines() {
        int userId = SessionManager.getInstance().getCurrentUser().getId();
        ObservableList<Routine> routines = DatabaseManager.getInstance().getRoutines(userId);
        routinesList.setItems(routines);
    }

    @FXML 
    public void onRoutineSelected(MouseEvent e) {
        Routine selected = routinesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            routineNameLabel.setText(selected.getName());
            routineDescLabel.setText(selected.getDescription());
            
            ObservableList<Exercise> exercises = DatabaseManager.getInstance().getRoutineExercises(selected.getId());
            routineExercisesList.setItems(exercises);
        } else {
            routineNameLabel.setText("Select a routine");
            routineDescLabel.setText("");
            routineExercisesList.setItems(null);
        }
    }

    @FXML 
    public void createNewRoutine(ActionEvent e) {
        Dialog<Routine> dialog = createRoutineDialog(null);
        Optional<Routine> result = dialog.showAndWait();
        result.ifPresent(routine -> {
            int userId = SessionManager.getInstance().getCurrentUser().getId();
            DatabaseManager.getInstance().createRoutine(userId, routine.getName(), routine.getDescription());
            loadRoutines();
        });
    }

    @FXML 
    public void editRoutine(ActionEvent e) {
        Routine selected = routinesList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Dialog<Routine> dialog = createRoutineDialog(selected);
        Optional<Routine> result = dialog.showAndWait();
        result.ifPresent(routine -> {
            DatabaseManager.getInstance().updateRoutine(selected.getId(), routine.getName(), routine.getDescription());
            loadRoutines();
            
            // re-select
            onRoutineSelected(null);
        });
    }

    private Dialog<Routine> createRoutineDialog(Routine existing) {
        Dialog<Routine> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "New Routine" : "Edit Routine");
        dialog.setHeaderText("Enter routine details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Routine Name");
        TextField descField = new TextField();
        descField.setPromptText("Description");

        if (existing != null) {
            nameField.setText(existing.getName());
            descField.setText(existing.getDescription());
        }

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(existing == null);

        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            saveButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Routine r = new Routine();
                r.setName(nameField.getText());
                r.setDescription(descField.getText());
                return r;
            }
            return null;
        });
        
        return dialog;
    }

    @FXML 
    public void deleteRoutine(ActionEvent e) {
        Routine selected = routinesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            DatabaseManager.getInstance().deleteRoutine(selected.getId());
            loadRoutines();
            onRoutineSelected(null);
        }
    }

    @FXML 
    public void addExerciseToRoutine(ActionEvent e) {
        Routine selected = routinesList.getSelectionModel().getSelectedItem();
        Exercise ex = addExerciseCombo.getSelectionModel().getSelectedItem();
        
        if (selected != null && ex != null) {
            int order = routineExercisesList.getItems().size();
            DatabaseManager.getInstance().addExerciseToRoutine(selected.getId(), ex.getId(), 3, 10, order);
            
            // Refresh list
            routineExercisesList.setItems(DatabaseManager.getInstance().getRoutineExercises(selected.getId()));
        }
    }

    @FXML 
    public void startWorkout(ActionEvent e) {
        Routine selected = routinesList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Workout Started");
            alert.setHeaderText("Starting: " + selected.getName());
            alert.setContentText("You have " + routineExercisesList.getItems().size() + " exercises in this routine.\n\n" +
                                 "Head over to the Log Workout tab to begin recording your sets!");
            alert.showAndWait();
        }
    }
}
