package com.carlfx.worldclock;

import com.jsoniter.output.JsonStream;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigLocationsController {
    @FXML
    public ComboBox<String> usStates;

    @FXML
    public TextField gmtOffset;

    @FXML
    public TextField city;

    @FXML
    public TextField countryCode;

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

    @FXML
    private Button saveLocationButton;

    @FXML
    private ListView<Location> locationsListView;

    private ObservableList<Location> locations;

    // Subscribers of Location add and remove will respond.
    private ListChangeListener<Location> listChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(Change<? extends Location> c) {
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                        System.out.println("todo permutate items ");
                    }
                } else if (c.wasUpdated()) {
                    //update item
                    System.out.println("todo update item !!!!" );
                } else {
                    for (Location rmItem : c.getRemoved()) {
                        removeListViewItem(rmItem);
                        WorldClockEvent.trigger(gmtOffset, WorldClockEvent.LOCATION_REMOVE, rmItem);
                    }
                    for (Location addItem : c.getAddedSubList()) {
                        updateListViewItem(addItem);
                        System.out.println("should not happen yet");
                        WorldClockEvent.trigger(longitude, WorldClockEvent.LOCATION_ADD, addItem);
                    }
                }
            }
        }
    };

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
    private void handleSaveLocationAction(ActionEvent actionEvent) {
        Location location = locationsListView.getSelectionModel().getSelectedItem();
        String stateSelected = usStates.getSelectionModel().getSelectedItem();
        String cityStr = "";
        if (!city.getText().isBlank()) {
            cityStr = city.getText();
        }

        if (location != null) {
            // update existing item
            if (location instanceof USLocation) {
                USLocation usLocation = (USLocation) location;
                usLocation.setState(stateSelected);
            }
            location.setCity(cityStr);
            locationsListView.refresh();
            if (!latitude.getText().isBlank() && !longitude.getText().isBlank()) {
                location.setLatLong(latitude.getText(), longitude.getText());
            }

        } else {

            if (stateSelected != null && !"".equals(stateSelected)) {
                location = new USLocation(gmtOffset.getText(), city.getText(), stateSelected);
            } else {
                location = new Location(gmtOffset.getText(), city.getText(), countryCode.getText());
            }

            // Add Latitude and longitude
            if (!latitude.getText().isBlank() && !longitude.getText().isBlank()) {
                location.setLatLong(latitude.getText(), longitude.getText());
            }
            if (!city.getText().isBlank()) {
                location.setCity(cityStr);
            }
            locations.add(location);
        }
        saveLocations();
    }

    private void saveLocations() {
        // check if file exists
        File file = new File(System.getProperty("user.home") + File.separatorChar + "worldclock");

        if (!file.exists()) {
            file.mkdirs();
        }
        File locationsJson = new File(String.format("%s%s%s%s%s",
                System.getProperty("user.home"),
                File.separatorChar,
                "worldclock",
                File.separatorChar,
                "locations.json"));
        try (FileWriter fileWriter = new FileWriter(locationsJson)){
            List<Location> locationList = locationsListView.getItems().stream().collect(Collectors.toList());
            String arrayJson = JsonStream.serialize(locationList);
            fileWriter.write(arrayJson);

            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleDeleteLocationAction(ActionEvent actionEvent) {
        Location location = locationsListView.getSelectionModel().getSelectedItem();
        if (location != null) {
            locations.remove(location);
        }
        saveLocations();
    }
    @FXML
    private void handleMoveUpLocationAction(ActionEvent actionEvent) {
        int index = locationsListView.getSelectionModel().getSelectedIndex();
        if (index > 0) {
            index = index - 1;
            Location prevLocation = locationsListView.getItems().get(index);
            Location location = locationsListView.getSelectionModel().getSelectedItem();
            locationsListView.getItems().set(index, location);
            locationsListView.getItems().set(index + 1, prevLocation);
            locationsListView.getSelectionModel().select(index);
            WorldClockEvent.trigger(gmtOffset, WorldClockEvent.LOCATION_MOVE_UP, new RowLocation(index, location));
        }
        saveLocations();
    }
    @FXML
    private void handleMoveDownLocationAction(ActionEvent actionEvent) {
        int index = locationsListView.getSelectionModel().getSelectedIndex();
        if (index > -1 && index < locationsListView.getItems().size() -1) {
            index = index + 1;
            Location nextLocation = locationsListView.getItems().get(index);
            Location location = locationsListView.getSelectionModel().getSelectedItem();
            locationsListView.getItems().set(index, location);
            locationsListView.getItems().set(index - 1, nextLocation);
            locationsListView.getSelectionModel().select(index);
            WorldClockEvent.trigger(gmtOffset, WorldClockEvent.LOCATION_MOVE_DOWN, new RowLocation(index, location));
        }
        saveLocations();
    }

    private void removeListViewItem(Location location) {
        Iterator<Location> itr = locationsListView.getItems().iterator();
        while (itr.hasNext()) {
            Location loc = itr.next();
            if (loc.equals(location)) {
                itr.remove();
            }
        }

    }

    private void updateListViewItem(Location updateOrAddlocation) {
        Iterator<Location> itr = locationsListView.getItems().iterator();
        int idx = -1;
        boolean found = false;

        while (itr.hasNext()) {
            Location loc = itr.next();
            idx+=1;
            if (loc.equals(updateOrAddlocation)) {
                found = true;
                break;
            }
        }
        if (found && locationsListView.getItems().size()-1 > 0) {
            // replace with new location
            locationsListView.getItems().set(idx, updateOrAddlocation);
        } else {
            locationsListView.getItems().add(updateOrAddlocation);
        }


    }

    private void clearForm() {
        usStates.getSelectionModel().select("");
        city.setText("");
        countryCode.setText("");
        gmtOffset.setText("");
        latitude.setText("");
        longitude.setText("");
    }
    private void populateForm(Location location) {
        if (location instanceof USLocation) {
            usStates.getSelectionModel().select(((USLocation)location).getState());
        }

        city.setText(location.getCity());
        countryCode.setText(location.getCountryCode());
        gmtOffset.setText(location.getTimezone());
        if (location.getLatLong() != null
                && location.getLatLong().length == 2) {
            latitude.setText(String.valueOf(location.getLatitude()));
            longitude.setText(String.valueOf(location.getLongitude()));
        }
    }

    @FXML
    private void handleAddLocationAction(ActionEvent actionEvent) {
        clearForm();
        locationsListView.getSelectionModel().clearSelection();
        city.requestFocus();
    }

    @FXML
    private void handleEditLocationAction(ActionEvent actionEvent) {
        clearForm();
        Location location = locationsListView.getSelectionModel().getSelectedItem();
        if (location != null) {
            populateForm(location);
        }
    }

    public void init (ObservableList<Location> xlocations) {
        locations = FXCollections.observableArrayList(xlocations);
        locations.addListener(listChangeListener);

        // Populate US states combo
        populateStates();

        // Set ListCell Factory
        locationsListView.setCellFactory(param -> {
            final Label label = new Label();
            final Tooltip tooltip = new Tooltip();
            return new ListCell<>(){
              @Override
              public void updateItem(Location location, boolean empty) {
                  super.updateItem(location, empty);
                  if (location != null) {
                      String row = location.getTimezone() + " " + location.getFullLocationName();
                      label.setText(row);
                      setText(row);
                      if (location.getLatLong() != null) {
                          tooltip.setText("%s \n GMT %s \nlat/lon (%.5f, %.5f)\n".formatted(location.getFullLocationName(), location.getTimezone(), location.getLatitude(), location.getLongitude()));
                      } else {
                          tooltip.setText("%s \n GMT %s \n".formatted(location.getFullLocationName(), location.getTimezone()));
                      }
                      setTooltip(tooltip);
                  } else {
                      setText("");
                  }
              }
            };
        });
        locationsListView.getItems().addAll(locations);
        addValidationRangeCheckInt(-12, 12, gmtOffset, gmtErrorOverlayIcon);
        addValidationRangeCheckDouble(-90, 90, latitude, latitudeErrorOverlayIcon);
        addValidationRangeCheckDouble(-180, 180, longitude, longitudeErrorOverlayIcon);

        // edit mode when selecting location ListView
        locationsListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            clearForm();
            if (newValue != null) {
                populateForm(newValue);
            }
        });
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
"", "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FM",
          "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
          "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
          "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH",
          "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN", "TX",
          "UT", "VI", "VA", "WA", "WV", "WI", "WY");
    }
}
