package com.gymlog.ui;

import com.gymlog.data.WorkoutRepository;
import com.gymlog.model.WorkoutEntry;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * "Log Workout" tab: a form (GridPane of common controls) for entering a
 * new set, plus a TableView of everything logged so far. Demonstrates
 * JavaFX event handling (button actions) and basic layout composition
 * (BorderPane containing a VBox of form + buttons on top, a TableView
 * in the center).
 */
public class LogWorkoutPane {

    private final WorkoutRepository repository;
    private final BorderPane view = new BorderPane();
    private final ObservableList<WorkoutEntry> tableData = FXCollections.observableArrayList();

    private final ComboBox<String> exerciseBox = new ComboBox<>();
    private final DatePicker datePicker = new DatePicker(LocalDate.now());
    private final TextField weightField = new TextField();
    private final Spinner<Integer> repsSpinner = new Spinner<>(1, 100, 8);
    private final Spinner<Integer> setsSpinner = new Spinner<>(1, 20, 3);
    private final TextField notesField = new TextField();
    private final Label errorLabel = new Label();

    private Runnable onEntryAdded = () -> { };

    public LogWorkoutPane(WorkoutRepository repository) {
        this.repository = repository;
        build();
        refreshTable();
    }

    public Parent getView() {
        return view;
    }

    /** Called after an entry is added or removed, so other tabs can refresh their exercise lists. */
    public void setOnEntryAdded(Runnable callback) {
        this.onEntryAdded = callback;
    }

    private void build() {
        exerciseBox.setEditable(true);
        exerciseBox.setItems(FXCollections.observableArrayList(repository.getKnownExerciseNames()));
        exerciseBox.setPromptText("Exercise name");
        weightField.setPromptText("Weight (kg)");
        notesField.setPromptText("Notes (optional)");
        repsSpinner.setEditable(true);
        setsSpinner.setEditable(true);
        errorLabel.getStyleClass().add("error-label");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);
        form.setPadding(new Insets(15));
        form.addRow(0, new Label("Exercise:"), exerciseBox, new Label("Date:"), datePicker);
        form.addRow(1, new Label("Weight (kg):"), weightField, new Label("Reps:"), repsSpinner);
        form.addRow(2, new Label("Sets:"), setsSpinner, new Label("Notes:"), notesField);

        Button addButton = new Button("Add Set");
        addButton.setDefaultButton(true);
        addButton.setTooltip(new Tooltip("Log this set to your history"));
        addButton.setOnAction(e -> handleAdd());

        TableView<WorkoutEntry> table = buildTable();

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setTooltip(new Tooltip("Remove the selected row from your log"));
        deleteButton.setOnAction(e -> {
            WorkoutEntry selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                repository.removeEntry(selected);
                refreshTable();
                onEntryAdded.run();
            }
        });

        HBox buttonRow = new HBox(10, addButton, deleteButton, errorLabel);
        buttonRow.setPadding(new Insets(0, 15, 10, 15));

        VBox top = new VBox(form, buttonRow);
        view.setTop(top);
        view.setCenter(table);
    }

    private TableView<WorkoutEntry> buildTable() {
        TableView<WorkoutEntry> table = new TableView<>(tableData);

        TableColumn<WorkoutEntry, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        TableColumn<WorkoutEntry, String> exerciseCol = new TableColumn<>("Exercise");
        exerciseCol.setCellValueFactory(new PropertyValueFactory<>("exerciseName"));
        exerciseCol.setPrefWidth(160);

        TableColumn<WorkoutEntry, Double> weightCol = new TableColumn<>("Weight (kg)");
        weightCol.setCellValueFactory(new PropertyValueFactory<>("weight"));

        TableColumn<WorkoutEntry, Integer> repsCol = new TableColumn<>("Reps");
        repsCol.setCellValueFactory(new PropertyValueFactory<>("reps"));

        TableColumn<WorkoutEntry, Integer> setsCol = new TableColumn<>("Sets");
        setsCol.setCellValueFactory(new PropertyValueFactory<>("sets"));

        TableColumn<WorkoutEntry, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(new PropertyValueFactory<>("notes"));
        notesCol.setPrefWidth(200);

        table.getColumns().addAll(dateCol, exerciseCol, weightCol, repsCol, setsCol, notesCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void handleAdd() {
        errorLabel.setText("");

        String exercise = exerciseBox.getEditor().getText();
        if (exercise == null || exercise.isBlank()) {
            exercise = exerciseBox.getValue();
        }
        if (exercise == null || exercise.isBlank()) {
            errorLabel.setText("Enter an exercise name.");
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightField.getText().trim());
        } catch (NumberFormatException ex) {
            errorLabel.setText("Weight must be a number.");
            return;
        }

        LocalDate date = datePicker.getValue() == null ? LocalDate.now() : datePicker.getValue();
        int reps = repsSpinner.getValue();
        int sets = setsSpinner.getValue();
        String notes = notesField.getText();

        WorkoutEntry entry = new WorkoutEntry(date, exercise.trim(), weight, reps, sets, notes);
        repository.addEntry(entry);

        if (!exerciseBox.getItems().contains(entry.getExerciseName())) {
            exerciseBox.getItems().add(entry.getExerciseName());
        }

        weightField.clear();
        notesField.clear();

        refreshTable();
        onEntryAdded.run();
    }

    private void refreshTable() {
        tableData.setAll(repository.getAllEntries());
    }
}
