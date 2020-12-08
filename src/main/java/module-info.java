module worldclock {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.carlfx.worldclock to javafx.fxml;
    exports com.carlfx.worldclock;
}