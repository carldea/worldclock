module worldclock {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    opens com.carlfx.worldclock to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, javafx.fxml;
    exports com.carlfx.worldclock;
}