package com.carlfx.worldclock;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import static com.carlfx.worldclock.WorldClockEvent.CONFIG_SHOWING;
import static com.carlfx.worldclock.WorldClockEvent.MAIN_APP_CLOSE;
public class WindowController {
    private boolean isConfigShowing = false;
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
            WorldClockEvent.trigger(closeButton, new WorldClockEvent(MAIN_APP_CLOSE, this));
        }
    }

    @FXML
    private void handleConfigWorldClockAction(MouseEvent mouseEvent) {
        if (configButton.equals(targetButton)) {
            System.out.println("config button hit " + mouseEvent);
            isConfigShowing = !isConfigShowing;
            WorldClockEvent.trigger(configButton, new WorldClockEvent(CONFIG_SHOWING, this));
        }
    }

    public boolean isConfigShowing() {
        return isConfigShowing;
    }

}
