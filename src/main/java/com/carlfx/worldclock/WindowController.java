package com.carlfx.worldclock;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class WindowController {
    @FXML
    private Button closeButton;

    @FXML
    private Button configButton;

    private ObservableList<Location> locations;

    public void init(ObservableList<Location> locations) {
        this.locations = locations;
    }

    private Object targetButton;

    @FXML
    private void handleEnterIngnoreAction(MouseEvent mouseEvent) {
        targetButton = mouseEvent.getTarget();
    }
    @FXML
    private void handleExitIngnoreAction(MouseEvent mouseEvent) {
        targetButton = null;
    }

    @FXML
    private void handleCloseWindowAction(MouseEvent mouseEvent) {
        if (closeButton.equals(targetButton)) {
            Stage stage = (Stage) closeButton
                    .getScene()
                    .getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleConfigWorldClockAction(MouseEvent mouseEvent) {
        if (configButton.equals(targetButton)) {
            System.out.println("config button hit " + mouseEvent);
        }
    }
}
