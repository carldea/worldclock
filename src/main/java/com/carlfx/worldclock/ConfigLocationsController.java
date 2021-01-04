package com.carlfx.worldclock;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ConfigLocationsController {
    @FXML
    public ComboBox<String> usStates;

    @FXML
    public TextField gmtOffset;

    @FXML
    public Button gmtErrorOverlayIcon;

    @FXML
    public TextField latitude;

    @FXML
    public Button latitudeErrorOverlayIcon;

    @FXML
    public TextField longitude;

    @FXML
    public Button longitudeErrorOverlayIcon;

    private ObservableList<Location> locations;

    public void init (ObservableList<Location> locations) {
        this.locations = locations;
        populateStates();

        addValidationRangeCheckInt(-12, 12, gmtOffset, gmtErrorOverlayIcon);
        addValidationRangeCheckDouble(-90, 90, latitude, latitudeErrorOverlayIcon);
        addValidationRangeCheckDouble(-180, 180, longitude, longitudeErrorOverlayIcon);

    }

    private void addValidationRangeCheckDouble(double min, double max, TextField field, Button errorOverlayIcon) {
        errorOverlayIcon.setVisible(false);
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String str = "0";
                if (!"".equals(newValue.trim())) {
                    str = newValue;
                }

                Double gmt = Double.parseDouble(str.trim());
                errorOverlayIcon.setVisible((gmt < min || gmt > max));
            } catch (Exception e) {
                errorOverlayIcon.setVisible(true);
            }

        });
    }

    private void addValidationRangeCheckInt(int min, int max, TextField field, Button errorOverlayIcon) {
        errorOverlayIcon.setVisible(false);
        field.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                String str = "0";
                if (!"".equals(newValue.trim())) {
                    str = newValue;
                }
                Integer value = Integer.parseInt(str.trim());
                errorOverlayIcon.setVisible((value < min || value > max));
            } catch (Exception e) {
                errorOverlayIcon.setVisible(true);
            }

        });
    }

    private void populateStates() {
        if (usStates == null ) {
            System.out.println("shouldn't be null!");
        }
        usStates.getItems().addAll(
"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FM",
          "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
          "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
          "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH",
          "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN", "TX",
          "UT", "VI", "VA", "WA", "WV", "WI", "WY");
    }
}
