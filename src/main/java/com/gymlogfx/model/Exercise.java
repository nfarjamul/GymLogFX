package com.gymlogfx.model;

import javafx.beans.property.*;

/**
 * Exercise model - represents an exercise entry
 * Demonstrates JavaFX properties for TableView binding
 */
public class Exercise {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty muscleGroup = new SimpleStringProperty();
    private final StringProperty equipment = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();

    public Exercise() {}

    public Exercise(int id, String name, String muscleGroup, String equipment, String description) {
        setId(id);
        setName(name);
        setMuscleGroup(muscleGroup);
        setEquipment(equipment);
        setDescription(description);
    }

    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int v) { id.set(v); }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v); }

    public StringProperty muscleGroupProperty() { return muscleGroup; }
    public String getMuscleGroup() { return muscleGroup.get(); }
    public void setMuscleGroup(String v) { muscleGroup.set(v); }

    public StringProperty equipmentProperty() { return equipment; }
    public String getEquipment() { return equipment.get(); }
    public void setEquipment(String v) { equipment.set(v); }

    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }

    @Override
    public String toString() { return getName(); }
}
