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

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.carlfx.worldclock.WorldClockEvent.*;

/**
 * JavaFX World Clock
 *
 * Creative commons attribution:
 * Font Awesome was used for buttons https://fontawesome.com/license
 */
public class App extends Application {

    private static Stage stage;
    String [] fontFiles = {
            "Roboto-Regular.ttf",
            "Roboto-Black.ttf",
            "Roboto-Bold.ttf",
            "Roboto-Light.ttf",
            "Roboto-Medium.ttf",
            "RobotoMono-Medium.ttf"
    };

    private final static String LCD_FONT_PROP = "prism.lcdtext";
    public static VBox clockList;

    @Override
    public void init() throws Exception {
        super.init();
        // load fonts
        Arrays.stream(fontFiles).forEach(this::fontLoader);
    }

    private void fontLoader(String fileName) {
        Font.loadFont(Objects.requireNonNull(App.class.getResource(fileName)).toExternalForm(), 20);
    }

    static ObservableList<Location> locations;


    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setOpacity(.75);
        App.stage = stage;

        BorderPane windowContainer = new BorderPane();
        windowContainer.getStyleClass().add("clock-background");
        windowContainer.addEventHandler(WorldClockEvent.MAIN_APP_CLOSE, event -> stage.close());

        // load the window title bar
        windowContainer.getStyleClass().add("window-container");
        clockList = new VBox();
        FXMLLoader titleBarControlLoader = new FXMLLoader(App.class.getResource("window-controls.fxml"));
        Parent windowBar = titleBarControlLoader.load();
        BorderPane.setAlignment(windowBar, Pos.CENTER_RIGHT);
        windowContainer.setTop(windowBar);

        // load the config form
        Pane centerPane = new Pane();
        FXMLLoader configLocationLoader = new FXMLLoader(App.class.getResource("config-locations.fxml"));
        Pane configPane = configLocationLoader.load();
        ConfigLocationsController configController = configLocationLoader.getController();

        // locations is a singleton from the config controller.
        locations = configController.getLocations();

        configPane.setVisible(false);
        ScrollPane scrollPane = new ScrollPane(clockList);
        scrollPane.getStyleClass().add("clock-background");

        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        centerPane.getChildren().addAll(scrollPane, configPane);

        scrollPane.prefHeightProperty().bind(centerPane.heightProperty());
        //makeDraggable(scrollPane);
        windowContainer.setCenter(centerPane);
        makeDraggable(windowContainer);

        // Create a WebView (index.html) containing a map. After map is finished loading add the pins of locations.
        WebView webView = createMap((webEngine) -> {
            String accessToken = loadAccessToken();
            String script = "init('" + accessToken + "')";
            webEngine.executeScript(script);
            for (Location location:locations) {
                // Call JS function addMarker to add pins to the map based on locations.
                webEngine.executeScript(
                        "addMarker(\"%s\", %3.8f, %3.8f)".formatted(location.getFullLocationName(),
                                location.getLatitude(), location.getLongitude()));
            }
            centerPane.setPrefHeight(configPane.getBoundsInLocal().getHeight());
        });

        // Obtain web engine
        final WebEngine webEngine = webView.getEngine();

        // Style the vbox
        clockList.getStyleClass().add("clock-background");
        clockList.getChildren()
                .addAll(locations.stream()
                        .map( location -> createOneClock(webEngine, location)) /* Create clock from FXML */
                        .toList());

        scrollPane.getStyleClass().add("clock-background");

        // Animate toggle between Config view vs World Clock List view
        windowContainer.addEventHandler(CONFIG_SHOWING, event -> {
            // Location list will slide out of view.
            TranslateTransition moveList = new TranslateTransition();
            moveList.setNode(scrollPane);
            moveList.setDuration(Duration.millis(400));
            WindowController windowController = event.getPayload();
            if (windowController.isConfigShowing()) {
                moveList.setInterpolator(Interpolator.EASE_OUT);
                moveList.setFromX(0);
                moveList.setToX(-scrollPane.getWidth());
            } else {
                moveList.setInterpolator(Interpolator.EASE_IN);
                moveList.setFromX(-scrollPane.getWidth());
                moveList.setToX(0);
            }

            // Config Pane slide in view
            TranslateTransition moveConfig = new TranslateTransition();
            moveConfig.setNode(configPane);
            moveConfig.setDuration(Duration.millis(400));
            if (!windowController.isConfigShowing()) {
                scrollPane.toFront();
                moveConfig.setInterpolator(Interpolator.EASE_OUT);
                moveConfig.setFromX(0);
                moveConfig.setToX(-scrollPane.getWidth() + scrollPane.getPadding().getLeft() + scrollPane.getPadding().getRight());
            } else {
                configPane.setVisible(true);
                configPane.toFront();
                moveConfig.setInterpolator(Interpolator.EASE_IN);
                moveConfig.setFromX(-scrollPane.getWidth() + scrollPane.getPadding().getLeft() + scrollPane.getPadding().getRight());
                moveConfig.setToX(0);
            }
            // also move bottom portion to size.
            // Create a Timeline for the animation
//            Timeline moveBottomTimeline = new Timeline();
//
//            if (!windowController.isConfigShowing()) {
//                // Create a KeyValue to set the height to the initial value
//                KeyFrame startFrame = new KeyFrame(Duration.ZERO, new KeyValue(centerPane.maxWidthProperty(), scrollPane.getHeight()));
//
//                // Create a KeyValue to set the height to the final value
//                KeyFrame endFrame = new KeyFrame(Duration.millis(400), new KeyValue(centerPane.maxWidthProperty(), configPane.heightProperty().get()));
//
//                // Add a KeyFrame to the timeline, linking the KeyValue with the duration
//                moveBottomTimeline.getKeyFrames().addAll(startFrame, endFrame);
//            } else {
//                // Create a KeyValue to set the height to the initial value
//                KeyFrame startFrame = new KeyFrame(Duration.ZERO, new KeyValue(centerPane.maxWidthProperty(), configPane.heightProperty().get()));
//
//                // Create a KeyValue to set the height to the final value
//                KeyFrame endFrame = new KeyFrame(Duration.millis(400), new KeyValue(centerPane.maxWidthProperty(), scrollPane.getHeight()));
//
//                // Add a KeyFrame to the timeline, linking the KeyValue with the duration
//                moveBottomTimeline.getKeyFrames().addAll(startFrame, endFrame);
//            }

//            System.out.println("clock list  width " + clockList.getWidth());
//            System.out.println("config pane width " + configPane.getBoundsInParent().getWidth());
//            System.out.println("window bar  width " + windowBar.getBoundsInParent().getWidth());
//            System.out.println(" image      width " + mapImage.getBoundsInParent().getWidth());
//            moveBottomTimeline.playFromStart();
            moveConfig.playFromStart();
            moveList.playFromStart();
        });

        // Subscribe to a new Location Added event
        windowContainer.addEventHandler(LOCATION_ADD, event -> {
            Location location = event.getPayload();
            // Add a map marker
            webEngine.executeScript("addMarker(\"%s\", %3.8f, %3.8f)".formatted(
                    location.getFullLocationName(), location.getLatitude(), location.getLongitude()));

            // Make VBox visual rows(HBox) are in the same order.
            Iterator<Node> itr = clockList.getChildren().iterator();
            int idx = -1;
            boolean found = false;
            while (itr.hasNext()) {
                Location loc = (Location) itr.next().getUserData();
                idx+=1;
                if (loc.equals(location)) {
                    found = true;
                    break;
                }
            }

            Parent oneClockView = createOneClock(webEngine, location);

            if (found && clockList.getChildren().size()-1 > 0) {
                // replace with new location
                clockList.getChildren().set(idx, oneClockView);
            } else {
                clockList.getChildren().add(oneClockView);
            }

        });

        // When location (lat/lon) changes remove old pin (marker) on map and replace
        windowContainer.addEventHandler(LOCATION_UPDATE, event -> {
            Location location = event.getPayload();
            // Add a map marker
            webEngine.executeScript("addMarker(\"%s\", %3.8f, %3.8f)".formatted(
                    location.getFullLocationName(), location.getLatitude(), location.getLongitude()));
        });

        // Subscribe to a removed Location event
        windowContainer.addEventFilter(LOCATION_REMOVE, event -> {
            Location location = event.getPayload();
            System.out.println("window container location_remove heard!");
            // Remove Pin a map marker
            webEngine.executeScript("removeMarker(\"%s\")".formatted(location.getFullLocationName()));

            Iterator<Node> itr = clockList.getChildren().iterator();
            while (itr.hasNext()) {
                Node oneClockView = itr.next();
                Location loc = (Location) oneClockView.getUserData();
                if (loc.equals(location)) {
                    oneClockView.fireEvent(new WorldClockEvent(CLEANUP_CLOCK, "Clean up"));
                    itr.remove();
                    break;
                }
            }

            //WorldClockEvent.trigger(clockList, event);
            //System.out.println("broadcast out to children");
        });

        // Subscribe to a MOVE UP Location event
        windowContainer.addEventFilter(LOCATION_MOVE_UP, event -> {
            RowLocation rowLocation = event.getPayload();
            System.out.println("window container location_move_up heard! index:" + rowLocation.getIndex() + " loc: " + rowLocation.getLocation().getFullLocationName());
            List<Node> copyList = new ArrayList<>(clockList.getChildren());
            clockList.getChildren().removeAll(copyList);
            Node prevNode = copyList.get(rowLocation.getIndex());
            Node currentNode = copyList.get(rowLocation.getIndex() + 1);
            copyList.set(rowLocation.getIndex(), currentNode);
            copyList.set(rowLocation.getIndex()+1, prevNode);
            clockList.getChildren().addAll(copyList);
        });

        // Subscribe to a MOVE UP Location event
        windowContainer.addEventFilter(LOCATION_MOVE_DOWN, event -> {
            RowLocation rowLocation = event.getPayload();
            System.out.println("window container location_move_down heard! index:" + rowLocation.getIndex() + " loc: " + rowLocation.getLocation().getFullLocationName());
            List<Node> copyList = new ArrayList<>(clockList.getChildren());
            clockList.getChildren().removeAll(copyList);
            Node nextNode = copyList.get(rowLocation.getIndex());
            Node currentNode = copyList.get(rowLocation.getIndex() - 1);
            copyList.set(rowLocation.getIndex(), currentNode);
            copyList.set(rowLocation.getIndex() - 1, nextNode);
            clockList.getChildren().addAll(copyList);
        });

        windowContainer.setBottom(webView);
        Scene scene = new Scene(windowContainer);
        scene.getStylesheets()
             .add(Objects.requireNonNull(getClass()
                     .getResource("styles.css"))
             .toExternalForm());
        
        scene.setFill(null);
        stage.setScene(scene);
        stage.show();
    }

    private Parent createOneClock(WebEngine webEngine, Location location) {
        Parent oneClockView = null;
        try {
            oneClockView = loadClockFXML(location);
            oneClockView.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    String jsCall = "viewMapLocation('%s')".formatted(location.getFullLocationName());
                    webEngine.executeScript(jsCall);
                }
            });
            return oneClockView;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Optional<Node> grabScrollBar(ScrollPane scrollPane, Orientation orientation) {
        for (Node node : scrollPane.lookupAll(".scroll-bar") ) {
            System.out.println(String.join(",", node.getStyleClass()));
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                    // Do something with the horizontal scroll bar
                    if (orientation == Orientation.HORIZONTAL) {
                        return Optional.of(scrollBar);
                    }
                }
                if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                    if (orientation == Orientation.VERTICAL) {
                        return Optional.of(scrollBar);
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * A simple WebView containing a web page.
     * @return
     */
    private WebView createMap(Consumer<WebEngine> addPointMarkers) {
        WebView webView = new WebView();
        webView.setPrefWidth(310);
        webView.setPrefHeight(310);
        WebEngine webEngine = webView.getEngine();
        // Allows Java code to talk to JavaScript code
        webEngine.setJavaScriptEnabled(true);
        webEngine.setOnAlert(webEvent -> {
            System.out.println("WebKit Alert: " + webEvent.getData());
        });
        webEngine.setOnError(webEvent -> {
            System.out.println("errors -----> " + webEvent);
        });

        webEngine
                .getLoadWorker()
                .stateProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        System.out.println("index.html loaded successfully");

                        addPointMarkers.accept(webEngine);
                    } else if (newValue == Worker.State.FAILED) {
                        System.out.println("errors -----> " + observable);
                    } else {
                        System.out.println("WebEngine state " + newValue);
                    }
                }
        );

        webEngine.load(getClass().getResource("index.html").toExternalForm());
        webView.requestFocus();

        return webView;
    }

    public static class DragContext {
        public Point2D anchorPt;
        public Point2D previousLocation;
    }

    private void makeDraggable(Node node) {
        DragContext dragContext = new DragContext();

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
            if (dragContext.previousLocation == null) {
                dragContext.previousLocation = new Point2D(stage.getX(), stage.getY());
            }
            dragContext.anchorPt = new Point2D(mouseEvent.getScreenX(), mouseEvent.getScreenY());
        });

        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            if (dragContext.anchorPt != null && dragContext.previousLocation != null) {
                stage.setX(dragContext.previousLocation.getX()
                        + mouseEvent.getScreenX()
                        - dragContext.anchorPt.getX());
                stage.setY(dragContext.previousLocation.getY()
                        + mouseEvent.getScreenY()
                        - dragContext.anchorPt.getY());
            }
        });

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> dragContext.previousLocation = new Point2D(stage.getX(), stage.getY()));
    }

    private Parent loadClockFXML(Location location) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("clock-widget.fxml"));
        Parent parent = fxmlLoader.load();

        parent.setUserData(location);
        WorldClockController controller = fxmlLoader.getController();
        parent.addEventHandler(CLEANUP_CLOCK, event -> controller.cleanup());
        controller.init(location);
        return parent;
    }


    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    private String loadAccessToken() {
        // load open street map api key
        String accessToken = "";
        try {
            accessToken = new String(Files.readAllBytes(Paths.get(getClass().getResource("openstreetmap-access-token.txt").toURI())));
            if (accessToken == null) {
                System.err.println("Error. No Access Token set a file called openstreetmap-access-token.txt.");
                throw new RuntimeException("Error. No Access Token set a file called openstreetmap-access-token.txt.");
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error loading access token from file openstreetmap-access-token.txt");
            alert.setHeaderText("Obtain Access Token from https://www.mapbox.com/ and save to file openstreetmap-access-token.txt");
            alert.setContentText("Register and create file openstreetmap-access-token.txt with your access token.\n" + e.getMessage());

            // Display the alert and wait for the user to close it
            Optional<ButtonType> result = alert.showAndWait();
            accessToken = "";
        }

        return accessToken;
    }

    public static void main(String[] args) {
        System.setProperty(LCD_FONT_PROP, "false");
        launch();
    }
}

/*
 Sunday    10:49
 March 19
 */