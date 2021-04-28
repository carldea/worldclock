/*
 * Copyright (c) 2021.
 *
 * This file is part of JFX World Clock.
 *
 *     JFX World Clock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     JFX World Clock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with JFX World Clock.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.carlfx.worldclock;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import static com.carlfx.worldclock.WorldClockEvent.CONFIG_SHOWING;
import static com.carlfx.worldclock.WorldClockEvent.MAIN_APP_CLOSE;

/**
 * A controller for the window-controls.fxml view to configure and close application.
 */
public class WindowController {
    private boolean isConfigShowing = false;
    @FXML
    private Button closeButton;

    @FXML
    private Button configButton;
    
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
            isConfigShowing = !isConfigShowing;
            WorldClockEvent.trigger(configButton, new WorldClockEvent(CONFIG_SHOWING, this));
        }
    }

    public boolean isConfigShowing() {
        return isConfigShowing;
    }

}
