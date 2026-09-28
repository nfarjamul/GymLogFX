package com.gymlogfx.model;

import javafx.beans.property.*;

/**
 * User model - demonstrates JavaFX properties and OOP encapsulation
 */
public class User {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty username = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty passwordHash = new SimpleStringProperty();
    private final StringProperty fitnessLevel = new SimpleStringProperty();
    private final StringProperty joinDate = new SimpleStringProperty();

    public User() {}

    public User(int id, String username, String email, String passwordHash, String fitnessLevel, String joinDate) {
        setId(id);
        setUsername(username);
        setEmail(email);
        setPasswordHash(passwordHash);
        setFitnessLevel(fitnessLevel);
        setJoinDate(joinDate);
    }

    // ID
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    // Username
    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public void setUsername(String value) { username.set(value); }

    // Email
    public StringProperty emailProperty() { return email; }
    public String getEmail() { return email.get(); }
    public void setEmail(String value) { email.set(value); }

    // Password Hash
    public StringProperty passwordHashProperty() { return passwordHash; }
    public String getPasswordHash() { return passwordHash.get(); }
    public void setPasswordHash(String value) { passwordHash.set(value); }

    // Fitness Level
    public StringProperty fitnessLevelProperty() { return fitnessLevel; }
    public String getFitnessLevel() { return fitnessLevel.get(); }
    public void setFitnessLevel(String value) { fitnessLevel.set(value); }

    // Join Date
    public StringProperty joinDateProperty() { return joinDate; }
    public String getJoinDate() { return joinDate.get(); }
    public void setJoinDate(String value) { joinDate.set(value); }

    @Override
    public String toString() {
        return "User{id=" + getId() + ", username='" + getUsername() + "', fitnessLevel='" + getFitnessLevel() + "'}";
    }
}
