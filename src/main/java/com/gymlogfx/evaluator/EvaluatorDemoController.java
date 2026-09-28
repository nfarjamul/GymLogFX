package com.gymlogfx.evaluator;

import com.gymlogfx.model.Person;
import com.gymlogfx.service.ApiService;
import com.gymlogfx.util.ThemeManager;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EvaluatorDemoController {
    // Tab 1 - Layout/Navigation
    @FXML public Button javaCodeButton;
    @FXML public Label clickedLabel;
    @FXML public TextField navTextField;
    @FXML public MenuBar menuBar;

    // Tab 2 - Data Controls
    @FXML public TableView<Person> personTable;
    @FXML public TableColumn<Person, String> colFirstName, colLastName, colEmail;
    @FXML public TableColumn<Person, Integer> colAge;
    @FXML public ListView<String> fruitListView, apiQuotesList;
    @FXML public TreeView<String> categoryTreeView;
    @FXML public Label selectedFruitLabel, selectedTreeLabel;

    // Tab 3 - Input Controls
    @FXML public PasswordField passwordDemoField;
    @FXML public TextField passwordVisible;
    @FXML public ChoiceBox<String> colorChoiceBox;
    @FXML public ToggleGroup genderGroup, fitnessGroup;
    @FXML public Label genderLabel, fitnessLabel, hobbiesLabel, dobLabel, colorTargetLabel, quantityLabel;
    @FXML public CheckBox readingCheck, gamingCheck, travelingCheck;
    @FXML public ComboBox<String> countryComboBox;
    @FXML public DatePicker dobPicker;
    @FXML public ColorPicker textColorPicker;
    @FXML public Spinner<Integer> quantitySpinner;

    // Tab 4 - Advanced
    @FXML public Slider fontSlider;
    @FXML public Label sliderTargetLabel;
    @FXML public TextArea noteTextArea;
    @FXML public TextField accumulateTarget;
    @FXML public ProgressBar accumulateProgressBar, apiProgressBar;
    @FXML public Label accumulateSumLabel, apiStatusDemoLabel, imageStatusLabel;
    @FXML public ImageView demoImageView;

    private int accumulateSum = 0;
    private boolean passwordShown = false;
    private final ApiService apiService = new ApiService();

    @FXML
    public void initialize() {
        // ── Java setOnAction demo ──
        javaCodeButton.setOnAction(e -> clickedLabel.setText("✅ Clicked via Java setOnAction!"));

        // ── TableView setup ──
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        personTable.setItems(FXCollections.observableArrayList(
            new Person("Alice", "Johnson", 28, "alice@gym.com"),
            new Person("Bob", "Smith", 34, "bob@gym.com"),
            new Person("Carol", "Williams", 22, "carol@gym.com"),
            new Person("David", "Brown", 41, "david@gym.com"),
            new Person("Eva", "Martinez", 30, "eva@gym.com")
        ));

        // ── ListView setup ──
        fruitListView.setItems(FXCollections.observableArrayList(
            "🍎 Apple", "🍌 Banana", "🍇 Grapes", "🍊 Orange",
            "🍓 Strawberry", "🥝 Kiwi", "🍑 Peach", "🍍 Pineapple"
        ));

        // ── TreeView setup ──
        TreeItem<String> root = new TreeItem<>("📁 All Categories");
        root.setExpanded(true);
        TreeItem<String> upper = new TreeItem<>("💪 Upper Body");
        upper.getChildren().addAll(new TreeItem<>("Chest"), new TreeItem<>("Back"), new TreeItem<>("Shoulders"));
        TreeItem<String> lower = new TreeItem<>("🦵 Lower Body");
        lower.getChildren().addAll(new TreeItem<>("Quads"), new TreeItem<>("Hamstrings"), new TreeItem<>("Calves"));
        TreeItem<String> core = new TreeItem<>("🔥 Core");
        core.getChildren().addAll(new TreeItem<>("Abs"), new TreeItem<>("Obliques"));
        root.getChildren().addAll(upper, lower, core);
        categoryTreeView.setRoot(root);

        // ── ChoiceBox for colors ──
        colorChoiceBox.setItems(FXCollections.observableArrayList("Default", "Deep Blue", "Dark Red", "Forest Green", "Dark Purple"));
        colorChoiceBox.getSelectionModel().select("Default");

        // ── Countries ComboBox ──
        countryComboBox.setItems(FXCollections.observableArrayList(
            "Bangladesh", "United States", "United Kingdom", "Canada",
            "Australia", "India", "Germany", "France", "Japan", "Brazil"
        ));

        // ── Slider listener ──
        fontSlider.valueProperty().addListener((obs, oldV, newV) ->
            sliderTargetLabel.setStyle("-fx-font-size: " + newV.intValue() + "px;")
        );

        // ── Accumulate bar init ──
        accumulateProgressBar.setProgress(0);
    }

    // ── Tab 1: Navigation & Events ──

    @FXML public void handleFxmlButton(ActionEvent e) {
        clickedLabel.setText("✅ Clicked via FXML onAction!");
    }

    @FXML public void handleNavTextEnter(ActionEvent e) { goToPage2(e); }

    @FXML public void goToPage2(ActionEvent e) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EvaluatorPage2.fxml"));
            Node view = loader.load();
            EvaluatorPage2Controller controller = loader.getController();
            String text = navTextField.getText().isEmpty() ? "(nothing entered)" : navTextField.getText();
            controller.setData(text);
            // Replace the content in the parent scene
            Scene scene = menuBar.getScene();
            Stage stage = (Stage) scene.getWindow();
            Scene page2Scene = new Scene(((javafx.scene.layout.Region) view), scene.getWidth(), scene.getHeight());
            ThemeManager.getInstance().registerScene(page2Scene);
            stage.setScene(page2Scene);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML public void menuNew(ActionEvent e) { showMenuAlert("New", "New file action triggered."); }
    @FXML public void menuOpen(ActionEvent e) { showMenuAlert("Open", "Open file action triggered."); }
    @FXML public void menuExit(ActionEvent e) { ((Stage) menuBar.getScene().getWindow()).close(); }
    @FXML public void menuToggleTheme(ActionEvent e) { ThemeManager.getInstance().toggleTheme(); }
    @FXML public void menuAbout(ActionEvent e) {
        showMenuAlert("About GymLogFX", "GymLogFX v1.0.0\n\nA JavaFX workout tracker demonstrating OOP, threading, database access, and API integration.");
    }

    private void showMenuAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ── Tab 2: Data Controls ──

    @FXML public void onFruitSelected(MouseEvent e) {
        String selected = fruitListView.getSelectionModel().getSelectedItem();
        if (selected != null) selectedFruitLabel.setText(selected);
    }

    @FXML public void onTreeNodeSelected(MouseEvent e) {
        TreeItem<String> selected = categoryTreeView.getSelectionModel().getSelectedItem();
        if (selected != null) selectedTreeLabel.setText(selected.getValue());
    }

    // ── Tab 3: Input Controls ──

    @FXML public void togglePasswordVisibility(ActionEvent e) {
        passwordShown = !passwordShown;
        if (passwordShown) {
            passwordVisible.setText(passwordDemoField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordDemoField.setVisible(false);
            passwordDemoField.setManaged(false);
        } else {
            passwordDemoField.setText(passwordVisible.getText());
            passwordDemoField.setVisible(true);
            passwordDemoField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
        }
    }

    @FXML public void onColorChoiceSelected(ActionEvent e) {
        String choice = colorChoiceBox.getSelectionModel().getSelectedItem();
        if (choice == null) return;
        String color = switch (choice) {
            case "Deep Blue"     -> "#0f3460";
            case "Dark Red"      -> "#3d0000";
            case "Forest Green"  -> "#003d00";
            case "Dark Purple"   -> "#1a0033";
            default              -> "#1a1a2e";
        };
        colorChoiceBox.getScene().getRoot().setStyle("-fx-background-color: " + color + ";");
    }

    @FXML public void onGenderSelected(ActionEvent e) {
        Toggle selected = genderGroup.getSelectedToggle();
        if (selected instanceof RadioButton rb) genderLabel.setText(rb.getText());
    }

    @FXML public void onFitnessSelected(ActionEvent e) {
        Toggle selected = fitnessGroup.getSelectedToggle();
        if (selected instanceof RadioButton rb) fitnessLabel.setText(rb.getText());
    }

    @FXML public void submitHobbies(ActionEvent e) {
        List<String> hobbies = new ArrayList<>();
        if (readingCheck.isSelected())  hobbies.add("Reading");
        if (gamingCheck.isSelected())   hobbies.add("Gaming");
        if (travelingCheck.isSelected()) hobbies.add("Traveling");
        hobbiesLabel.setText("Selected: " + (hobbies.isEmpty() ? "(none)" : String.join(", ", hobbies)));
    }

    @FXML public void onDobSelected(ActionEvent e) {
        if (dobPicker.getValue() != null)
            dobLabel.setText("DOB: " + dobPicker.getValue().toString());
    }

    @FXML public void onColorPicked(ActionEvent e) {
        Color c = textColorPicker.getValue();
        String hex = String.format("#%02x%02x%02x",
            (int)(c.getRed() * 255), (int)(c.getGreen() * 255), (int)(c.getBlue() * 255));
        colorTargetLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + hex + ";");
    }

    @FXML public void showQuantity(ActionEvent e) {
        quantityLabel.setText("Quantity: " + quantitySpinner.getValue());
    }

    // ── Tab 4: Advanced ──

    @FXML public void clearNoteArea(ActionEvent e) { noteTextArea.clear(); }

    @FXML public void handleAccumulate(ActionEvent e) {
        int target = 5;
        try { target = Integer.parseInt(accumulateTarget.getText()); } catch (NumberFormatException ignored) {}
        accumulateSum++;
        double progress = Math.min((double) accumulateSum / target, 1.0);
        accumulateProgressBar.setProgress(progress);
        accumulateSumLabel.setText("Current Sum: " + accumulateSum + " / Target: " + target);
    }

    @FXML public void resetAccumulate(ActionEvent e) {
        accumulateSum = 0;
        accumulateProgressBar.setProgress(0);
        String target = accumulateTarget.getText().isEmpty() ? "5" : accumulateTarget.getText();
        accumulateSumLabel.setText("Current Sum: 0 / Target: " + target);
    }

    @FXML public void changeImage(ActionEvent e) {
        // Cycle through some online placeholder images
        String[] urls = {
            "https://picsum.photos/200/150?random=1",
            "https://picsum.photos/200/150?random=2",
            "https://picsum.photos/200/150?random=3"
        };
        String url = urls[(int)(Math.random() * urls.length)];
        try {
            demoImageView.setImage(new Image(url, true));
            imageStatusLabel.setText("Loaded random image.");
        } catch (Exception ex) {
            imageStatusLabel.setText("Failed to load image.");
        }
    }

    @FXML public void browseImage(ActionEvent e) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select an Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File file = fc.showOpenDialog(demoImageView.getScene().getWindow());
        if (file != null) {
            demoImageView.setImage(new Image(file.toURI().toString()));
            imageStatusLabel.setText("Loaded: " + file.getName());
        }
    }

    @FXML public void showInfoAlert(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "This is an information message.", ButtonType.OK);
        a.setTitle("Information"); a.setHeaderText("ℹ️ Info Alert"); a.showAndWait();
    }

    @FXML public void showWarningAlert(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.WARNING, "This is a warning message.", ButtonType.OK);
        a.setTitle("Warning"); a.setHeaderText("⚠️ Warning Alert"); a.showAndWait();
    }

    @FXML public void showErrorAlert(ActionEvent e) {
        Alert a = new Alert(Alert.AlertType.ERROR, "This is an error message.", ButtonType.OK);
        a.setTitle("Error"); a.setHeaderText("❌ Error Alert"); a.showAndWait();
    }

    @FXML public void fetchQuotesDemo(ActionEvent e) {
        apiProgressBar.setVisible(true);
        apiProgressBar.setProgress(-1); // indeterminate
        apiStatusDemoLabel.setText("Fetching...");
        apiQuotesList.getItems().clear();

        Task<List<String>> task = apiService.createQuoteFetchTask();
        apiStatusDemoLabel.textProperty().bind(task.messageProperty());

        task.setOnSucceeded(ev -> {
            apiQuotesList.getItems().addAll(task.getValue());
            apiProgressBar.setVisible(false);
            apiStatusDemoLabel.textProperty().unbind();
            apiStatusDemoLabel.setText("✅ Done!");
        });
        task.setOnFailed(ev -> {
            apiProgressBar.setVisible(false);
            apiStatusDemoLabel.textProperty().unbind();
            apiStatusDemoLabel.setText("❌ Failed");
        });

        new Thread(task).start();
    }
}

