module worldclock {
    requires javafx.controls;
    requires javafx.fxml;
    requires jsoniter;

    opens com.carlfx.worldclock to jsoniter,  javafx.fxml;
    exports com.carlfx.worldclock;
}