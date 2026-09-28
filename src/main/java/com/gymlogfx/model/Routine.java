package com.gymlogfx.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Routine - a named workout plan composed of exercises
 */
public class Routine {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty createdDate = new SimpleStringProperty();
    private final ObservableList<Exercise> exercises = FXCollections.observableArrayList();

    public Routine() {}

    public Routine(int id, int userId, String name, String description, String createdDate) {
        setId(id);
        setUserId(userId);
        setName(name);
        setDescription(description);
        setCreatedDate(createdDate);
    }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public IntegerProperty userIdProperty() { return userId; }
    public int getUserId() { return userId.get(); }
    public void setUserId(int v) { userId.set(v); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }

    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }

    public StringProperty createdDateProperty() { return createdDate; }
    public String getCreatedDate() { return createdDate.get(); }
    public void setCreatedDate(String v) { createdDate.set(v); }

    public ObservableList<Exercise> getExercises() { return exercises; }
    public void addExercise(Exercise e) { exercises.add(e); }

    @Override
    public String toString() { return getName(); }
}
