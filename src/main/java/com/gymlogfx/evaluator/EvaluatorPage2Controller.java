package com.gymlogfx.evaluator;

import com.gymlogfx.util.ThemeManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class EvaluatorPage2Controller {
    @FXML public Label receivedDataLabel;

    /** Called by Page 1 before the scene is shown — demonstrates inter-controller data passing */
    public void setData(String data) {
        if (receivedDataLabel != null) {
            receivedDataLabel.setText(data);
        }
    }

    @FXML public void initialize() {
        // receivedDataLabel may not have data yet — setData() is called after load()
    }

    @FXML public void goBack(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluatorDemoView.fxml"));
            Node view = loader.load();
            Scene currentScene = receivedDataLabel.getScene();
            Stage stage = (Stage) currentScene.getWindow();
            Scene page1Scene = new Scene(((javafx.scene.layout.Region) view), currentScene.getWidth(), currentScene.getHeight());
            ThemeManager.getInstance().registerScene(page1Scene);
            stage.setScene(page1Scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

