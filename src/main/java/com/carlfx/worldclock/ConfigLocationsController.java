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

import static java.lang.System.out;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The controller code for the config-locations.fxml file.
 */
public class ConfigLocationsController {
    @FXML
    public ComboBox<String> usStates;

    @FXML
    public ComboBox<TimeZoneRecord> timeZoneComboBox;

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

    private Object targetButton;

    private final static List<TimeZoneRecord> timeZoneRecords = new ArrayList<>();

    public ConfigLocationsController() {

    }
    @FXML
    public void initialize () {
        setupTimeZones();


        // Provides search suggestions
//        new AutoCompleteComboBoxListener<TimeZoneRecord>(timeZoneComboBox);


        locations = FXCollections.observableArrayList();

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
        if (locationsJson.exists()) {
            try {
                String actual = Files.readString(locationsJson.toPath());
                if (!"".equals(actual)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    List<Map<String, Object>> locationArray = objectMapper.readValue(actual, List.class);
                    for (Map<String, Object> map:locationArray) {
                        try {
                            Location location = null;

                            if (map.containsKey("state")) {
                                location = objectMapper.convertValue(map, USLocation.class);
                            } else {
                                location = objectMapper.convertValue(map, Location.class);
                            }
                            locations.add(location);

                        } catch (Throwable th) {
                            th.printStackTrace();
                        }
                    }
                }

                out.println("Successfully read from file.");
            } catch (IOException e) {
                out.println("An error occurred.");
                e.printStackTrace();
            }
        }

        if (locations.isEmpty()) {
            TimeZone tz = TimeZone.getDefault();
            String offsetId = "" + tz.toZoneId().getRules().getOffset(Instant.now()).getTotalSeconds()/60/60;

            String city = "Home";
            String state = "";

            String countryCode = Locale.getDefault().getCountry();
            if ("US".equalsIgnoreCase(countryCode)) {
                USLocation usLocation = new USLocation(offsetId, city, state, 3.3f, Location.TEMP_STD.CELSIUS);
                locations.add(usLocation);
            } else {
                String zoneId = tz.getID();
                if (zoneId.lastIndexOf("/") > -1) {
                    city = zoneId.substring(zoneId.lastIndexOf("/") + 1);
                } else {
                    city = zoneId;
                }
                locations.add(new Location(offsetId, city, countryCode, 3.3f, Location.TEMP_STD.CELSIUS));
            }
        }

        locations.addListener(listChangeListener);

        // Populate US states combo
        setupUSStates();

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
        //addValidationRangeCheckInt(-12, 12, gmtOffset, gmtErrorOverlayIcon);
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

    private void setupTimeZones() {
        // Load Time zone file
        synchronized (timeZoneRecords) {
            if (timeZoneRecords.isEmpty()) {
                // load them up
                loadTimeZoneRecords();
            }
        }

        // Show how each item will display in popup list view cell
        timeZoneComboBox.setCellFactory(new Callback<>() {
            @Override
            public ListCell<TimeZoneRecord> call(ListView<TimeZoneRecord> p) {
                return new ListCell<>() {
                    {
                        setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }

                    @Override
                    protected void updateItem(TimeZoneRecord item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item==null || empty) {
                            setGraphic(null);
                            setText("");
                            setTooltip(null);
                        } else {
                            setTooltip(new Tooltip(item.description()));
                            setText(String.valueOf(item.timeZone()).replaceAll("_", " "));
                            setGraphic(null);
                        }
                    }
                };
            }
        });

        // Show how combobox displays selected item.
        timeZoneComboBox.setConverter(new StringConverter<>() {

            @Override
            public String toString(TimeZoneRecord timeZoneRecord) {
                if (timeZoneRecord==null) {
                    return "";
                }
                return timeZoneRecord.timeZone();
            }

            @Override
            public TimeZoneRecord fromString(String s) {
                return findTimeZoneRecord(s).get();
            }
        });

        // populate with items
        timeZoneComboBox.getItems().addAll(getTimeZoneRecords());

        // add type ahead search.
        StringBuilder sb = new StringBuilder();
        List<Timeline> timersTasks = new CopyOnWriteArrayList<>();
        timeZoneComboBox.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // store key strokes cancel timer add to buffer
            if (event.getText().equals(" ")) {
                timeZoneComboBox.show();
                return;
            }
            // if control characters just let events bubble up
            if (event.getText().trim().isBlank()) return;

            // if valid text add to buffer
            sb.append(event.getText());
            // kill pending scheduled
            stopTasks(timersTasks);
        });

        // Whenever the popup shows display scroll to the selected item.
        timeZoneComboBox.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // The dropdown is showing
                out.println("Dropdown is showing");
                updateComboBoxSelectedItem(timeZoneComboBox);
            } else {
                out.println("Dropdown is hidden");
            }
        });

        // After user releases key press query and select item.
        timeZoneComboBox.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getText().trim().isBlank()) return;
            stopTasks(timersTasks);
            Timeline timerTask = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(600), "find Time Zone Id", (actionEvent -> {
                String state = sb.toString();
                out.println("Running " + state);
                for (TimeZoneRecord item : timeZoneComboBox.getItems()) {
                    if (timerTask.statusProperty().get().equals(Animation.Status.STOPPED)) break;
                    if (item.timeZone().toLowerCase().contains(state.toLowerCase())) {
                        Platform.runLater(() -> {
                            timeZoneComboBox.getSelectionModel().select(item);
                            timeZoneComboBox.setValue(item);
                            sb.setLength(0);
                            updateComboBoxSelectedItem(timeZoneComboBox);
                        });
                        break;
                    }
                }
            }));
            timerTask.getKeyFrames().add(keyFrame);
            timerTask.playFromStart();
            timersTasks.add(timerTask);
        });

    }

    private String replaceUnderScore(String input) {
        return input.replace("_", " ");
    }
    // Subscribers of Location add and remove will respond.
    private ListChangeListener<Location> listChangeListener = new ListChangeListener<>() {
        @Override
        public void onChanged(Change<? extends Location> c) {
            while (c.next()) {
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                        out.println("todo permutate items ");
                    }
                } else if (c.wasUpdated()) {
                    //update item
                    out.println("todo update item !!!!" );
                } else {
                    for (Location rmItem : c.getRemoved()) {
                        removeListViewItem(rmItem);
                        WorldClockEvent.trigger(timeZoneComboBox, WorldClockEvent.LOCATION_REMOVE, rmItem);
                    }
                    for (Location addItem : c.getAddedSubList()) {
                        updateListViewItem(addItem);
                        out.println("should not happen yet");
                        WorldClockEvent.trigger(longitude, WorldClockEvent.LOCATION_ADD, addItem);
                    }
                }
            }
        }
    };

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
            WorldClockEvent.trigger(longitude, WorldClockEvent.LOCATION_UPDATE, location);
        } else {
            String timezone = ZoneId.systemDefault().toString();
            if (timeZoneComboBox.getSelectionModel().getSelectedItem() != null) {
                timezone = timeZoneComboBox.getSelectionModel().getSelectedItem().timeZone();
            }
            if (stateSelected != null && !"".equals(stateSelected)) {
                location = new USLocation(timezone, city.getText(), stateSelected);
            } else {
                location = new Location(timezone, city.getText(), countryCode.getText());
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
            ObjectMapper objectMapper = new ObjectMapper();
            List<Location> locationList = locationsListView.getItems();
            objectMapper.writeValue(fileWriter, locationList);
            out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            out.println("An error occurred.");
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
            WorldClockEvent.trigger(timeZoneComboBox, WorldClockEvent.LOCATION_MOVE_UP, new RowLocation(index, location));
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
            WorldClockEvent.trigger(timeZoneComboBox, WorldClockEvent.LOCATION_MOVE_DOWN, new RowLocation(index, location));
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
        timeZoneComboBox.getSelectionModel().clearSelection();
        latitude.setText("");
        longitude.setText("");
    }
    private void populateForm(Location location) {
        if (location instanceof USLocation) {
            usStates.getSelectionModel().select(((USLocation)location).getState());
        }

        city.setText(location.getCity());
        countryCode.setText(location.getCountryCode());
        timeZoneComboBox.setValue(findTimeZoneRecord(location.getTimezone()).get());
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

    public ObservableList<Location> getLocations() {
        return locations;
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
    private void stopTasks(List<Timeline> timersTasks) {
        timersTasks.forEach(timerTask -> timerTask.stop());
        timersTasks.clear();
    }
    private void setupUSStates() {
        assert  usStates != null;
        usStates.getItems().addAll(
"", "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FM",
          "FL", "GA", "GU", "HI", "ID", "IL", "IN", "IA", "KS", "KY",
          "LA", "ME", "MH", "MD", "MA", "MI", "MN", "MS", "MO", "MT",
          "NE", "NV", "NH", "NJ", "NM", "NY", "NC", "ND", "MP", "OH",
          "OK", "OR", "PW", "PA", "PR", "RI", "SC", "SD", "TN", "TX",
          "UT", "VI", "VA", "WA", "WV", "WI", "WY");
        StringBuilder sb = new StringBuilder();
        List<Timeline> timersTasks = new CopyOnWriteArrayList<>();
        usStates.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            // store key strokes cancel timer add to buffer
            if (event.getText().equals(" ")) {
                usStates.show();
                return;
            }
            // if control characters just let events bubble up
            if (event.getText().trim().isBlank()) return;

            // if valid text add to buffer
            sb.append(event.getText());
            // kill pending scheduled
            stopTasks(timersTasks);
        });
        usStates.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!"".equals(newValue)) {
                countryCode.setText("US");
                countryCode.setDisable(true);
            } else {
                countryCode.setDisable(false);
            }
        });

        // Whenever the popup shows display scroll to the selected item.
        usStates.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // The dropdown is showing
                out.println("Dropdown is showing");
                updateComboBoxSelectedItem(usStates);
            } else {
                out.println("Dropdown is hidden");
            }
        });

        // After user releases key press query and select item.
        usStates.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (event.getText().trim().isBlank()) return;
            stopTasks(timersTasks);
            Timeline timerTask = new Timeline();
            KeyFrame keyFrame = new KeyFrame(Duration.millis(400), "find US state", (actionEvent -> {
                String state = sb.toString();
                out.println("Running " + state);
                for (String item : usStates.getItems()) {
                    if (timerTask.statusProperty().get().equals(Animation.Status.STOPPED)) break;
                    if (item.toLowerCase().startsWith(state.toLowerCase())) {
                        Platform.runLater(() -> {
                            usStates.getSelectionModel().select(item);
                            usStates.setValue(item);
                            sb.setLength(0);
                            updateComboBoxSelectedItem(usStates);
                        });
                        break;
                    }
                }
            }));
            timerTask.getKeyFrames().add(keyFrame);
            timerTask.playFromStart();
            timersTasks.add(timerTask);
        });
    }
    private void updateComboBoxSelectedItem(ComboBox comboBox) {
        if (comboBox.getSkin() instanceof ComboBoxListViewSkin skin) {
            Node popup = skin.getPopupContent();
            out.println("Dropdown is popup" + popup.getClass().getName() + popup);
            if (popup instanceof ListView<?> listView) {
                // Now you can work with the popup, e.g., get its content, style it, etc.
                System.out.println("ComboBox popup is visible!");
                int selectedIndex = comboBox.getSelectionModel().getSelectedIndex();
                listView.scrollTo(selectedIndex);
            }
        }
    }

    /**
     * Based on a time zone name retrieve one record.
     * @param timezone string of the timezone name.
     * @return TimeZoneRecord Timezone record has a name and description.
     */
    private Optional<TimeZoneRecord> findTimeZoneRecord(String timezone) {
        for (TimeZoneRecord timeZoneRecord : getTimeZoneRecords()) {
            if (timeZoneRecord.timeZone().equals(timezone)) {
                return Optional.of(timeZoneRecord);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns a list of TimeZoneRecord objects.
     * @return Returns a list of TimeZoneRecord objects.
     */
    private List<TimeZoneRecord> getTimeZoneRecords() {
        return timeZoneRecords;
    }

    private void loadTimeZoneRecords() {
        String fileName = "time-zones.csv";

        try (InputStream inputStream = ConfigLocationsController.class.getResourceAsStream(fileName);
             InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            if (inputStream == null) {
                System.err.println("File not found: " + fileName);
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    timeZoneRecords.add(new TimeZoneRecord(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
