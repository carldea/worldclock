package com.carlfx.worldclock;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * JavaFX World Clock
 *
 * Creative commons attribution:
 * Font Awesome was used for buttons https://fontawesome.com/license
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;
    String [] fontFiles = {
            "Roboto-Black.ttf",
            "Roboto-Bold.ttf",
            "Roboto-Light.ttf",
            "Roboto-Medium.ttf",
            "RobotoMono-Medium.ttf"
    };

    @Override
    public void init() throws Exception {
        super.init();
        Arrays.stream(fontFiles).forEach( f -> fontLoader(f));
    }
    private void fontLoader(String fileName) {
        Font.loadFont(App.class.getResource(fileName).toExternalForm(), 20);
    }
    private <T> T lookup(Node node, String id) {
       T childNode = (T) node.lookup("#"+id);
       return childNode;
    }
    static ObservableList<Location> locations = FXCollections.observableArrayList();


    @Override
    public void start(Stage stage) throws IOException {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setOpacity(.75);
        this.stage = stage;

        // fake data
        locations.addAll(
                new USLocation("GMT-5", "Pasadena, MD", "US", 32.0f, Location.TEMP_STD.FAHRENHEIT),
                new USLocation("GMT-8", "Sunnyvale, CA", "US", 60.0f, Location.TEMP_STD.FAHRENHEIT),
                new Location("GMT+1", "Amsterdam", "NL", 4.0f, Location.TEMP_STD.CELSIUS),
                new Location("GMT+1", "Münster", "DE",5.0f, Location.TEMP_STD.CELSIUS)
        );

        SimpleLongProperty epochTime = new SimpleLongProperty(new Date().getTime());
        // each tick update epoch property
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), /* every second */
                actionEvent -> epochTime.set(System.currentTimeMillis())) /* update epoch */
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        List<Parent> clocks = new ArrayList<>();
        for(Location location:locations) {
            // each controller will attach a listener when epochTime changes.
            clocks.add(loadClockFXML(location, epochTime));
        }
        BorderPane windowContainer = new BorderPane();
        windowContainer.getStyleClass().add("window-container");
        VBox clockList = new VBox();
        Parent windowBar = loadWindowControlsFXML(locations);
        BorderPane.setAlignment(windowBar, Pos.CENTER_RIGHT);
        windowContainer.setTop(windowBar);
        windowContainer.setCenter(clockList);
        makeDraggable(windowContainer);
        // fake map
        ImageView mapImage = new ImageView(new Image(App.class.getResourceAsStream("Mapimage.png")));

        clockList.getStyleClass().add("clock-background");
        clockList.getChildren()
                .addAll(clocks);
        clockList.getChildren()
                .add(mapImage);

        scene = new Scene(windowContainer);
        scene.getStylesheets()
             .add(getClass()
             .getResource("styles.css")
             .toExternalForm());

        stage.setScene(scene);
        stage.show();
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

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            dragContext.previousLocation = new Point2D(stage.getX(), stage.getY());
        });
    }

    private static Parent loadClockFXML(Location location, LongProperty epochTime) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("clock-widget.fxml"));
        Parent parent = fxmlLoader.load();
        WorldClockController controller = fxmlLoader.getController();
        controller.init(location, epochTime);
        return parent;
    }
    private static Parent loadWindowControlsFXML(ObservableList<Location> locations) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("window-controls.fxml"));
        Parent parent = fxmlLoader.load();
        WindowController controller = fxmlLoader.getController();
        controller.init(locations);
        return parent;
    }

    @Override
    public void stop() {
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        System.setProperty("prism.lcdtext", "false");
        launch();
    }
}

/**
 Sunday    10:49
 March 19
 */