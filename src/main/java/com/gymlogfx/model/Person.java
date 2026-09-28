package com.gymlogfx.model;

import javafx.beans.property.*;

/**
 * Person model - used in the Evaluator Demo TableView
 * Demonstrates JavaFX observable properties for table binding
 */
public class Person {
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final IntegerProperty age = new SimpleIntegerProperty();
    private final StringProperty email = new SimpleStringProperty();

    public Person() {}

    public Person(String firstName, String lastName, int age, String email) {
        setFirstName(firstName);
        setLastName(lastName);
        setAge(age);
        setEmail(email);
    }

    public StringProperty firstNameProperty() { return firstName; }
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String v) { firstName.set(v); }

    public StringProperty lastNameProperty() { return lastName; }
    public String getLastName() { return lastName.get(); }
    public void setLastName(String v) { lastName.set(v); }

    public IntegerProperty ageProperty() { return age; }
    public int getAge() { return age.get(); }
    public void setAge(int v) { age.set(v); }

    public StringProperty emailProperty() { return email; }
    public String getEmail() { return email.get(); }
    public void setEmail(String v) { email.set(v); }
}
