module com.gymlogfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;


    opens com.gymlogfx to javafx.fxml;
    opens com.gymlogfx.controller to javafx.fxml;
    opens com.gymlogfx.model to javafx.base;
    opens com.gymlogfx.evaluator to javafx.fxml;

    exports com.gymlogfx;
    exports com.gymlogfx.controller;
    exports com.gymlogfx.model;
    exports com.gymlogfx.evaluator;
    exports com.gymlogfx.database;
    exports com.gymlogfx.service;
    exports com.gymlogfx.util;
}
