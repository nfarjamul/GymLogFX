package com.gymlog.ui;

import com.gymlog.data.WorkoutRepository;
import com.gymlog.model.Routine;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * "Routines" tab: build named, reusable routines from the set of exercises
 * seen so far. Demonstrates ListView with multiple selection and
 * cross-control event wiring.
 */
public class RoutineBuilderPane {

    private final WorkoutRepository repository;
    private final BorderPane view = new BorderPane();

    private final ObservableList<Routine> routineData = FXCollections.observableArrayList();
    private final ListView<Routine> routineList = new ListView<>(routineData);

    private final ObservableList<String> availableExercises = FXCollections.observableArrayList();
    private final ListView<String> exercisePicker = new ListView<>(availableExercises);

    private final TextField nameField = new TextField();
    private final ObservableList<String> chosenExercises = FXCollections.observableArrayList();
    private final ListView<String> chosenList = new ListView<>(chosenExercises);

    public RoutineBuilderPane(WorkoutRepository repository) {
        this.repository = repository;
        exercisePicker.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        build();
        refresh();
    }

    public Parent getView() {
        return view;
    }

    private void build() {
        // --- Left: existing routines ---
        routineList.setPrefWidth(220);
        Button deleteRoutineButton = new Button("Delete Routine");
        deleteRoutineButton.setOnAction(e -> {
            Routine selected = routineList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                repository.removeRoutine(selected);
                refresh();
            }
        });
        routineList.getSelectionModel().selectedItemProperty().addListener((obs, old, routine) -> {
            if (routine != null) {
                nameField.setText(routine.getName());
                chosenExercises.setAll(routine.getExercises());
            }
        });

        VBox left = new VBox(8, new Label("Saved Routines"), routineList, deleteRoutineButton);
        left.setPadding(new Insets(15));

        // --- Right: builder form ---
        nameField.setPromptText("Routine name, e.g. Push Day");

        Button addExerciseButton = new Button("Add \u2192");
        addExerciseButton.setOnAction(e -> {
            for (String ex : exercisePicker.getSelectionModel().getSelectedItems()) {
                if (!chosenExercises.contains(ex)) chosenExercises.add(ex);
            }
        });

        Button removeExerciseButton = new Button("\u2190 Remove");
        removeExerciseButton.setOnAction(e -> {
            String selected = chosenList.getSelectionModel().getSelectedItem();
            if (selected != null) chosenExercises.remove(selected);
        });

        Button saveButton = new Button("Save Routine");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(e -> handleSave());

        Button newButton = new Button("New");
        newButton.setOnAction(e -> {
            routineList.getSelectionModel().clearSelection();
            nameField.clear();
            chosenExercises.clear();
        });

        VBox pickerBox = new VBox(6, new Label("Available Exercises"), exercisePicker, addExerciseButton);
        VBox chosenBox = new VBox(6, new Label("In Routine"), chosenList, removeExerciseButton);
        HBox pickerRow = new HBox(15, pickerBox, chosenBox);

        HBox nameRow = new HBox(10, new Label("Name:"), nameField, newButton, saveButton);
        nameRow.setPadding(new Insets(0, 0, 10, 0));

        VBox right = new VBox(12, nameRow, pickerRow);
        right.setPadding(new Insets(15));

        view.setLeft(left);
        view.setCenter(right);
    }

    private void handleSave() {
        String name = nameField.getText();
        if (name == null || name.isBlank()) return;

        Routine existing = routineList.getSelectionModel().getSelectedItem();
        if (existing != null && existing.getName().equals(name)) {
            repository.removeRoutine(existing);
        }
        Routine routine = new Routine(name.trim(), List.copyOf(chosenExercises));
        repository.addRoutine(routine);
        refresh();
    }

    /** Reloads both the routine list and the available-exercises picker from the repository. */
    public void refresh() {
        routineData.setAll(repository.getRoutines());
        availableExercises.setAll(repository.getKnownExerciseNames());
    }
}
