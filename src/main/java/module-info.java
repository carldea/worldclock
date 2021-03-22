module worldclock {
    requires javafx.controls;
    requires javafx.fxml;
    requires jsoniter;

    opens com.carlfx.worldclock to com.jsoniter,  javafx.fxml;
    exports com.carlfx.worldclock;
}