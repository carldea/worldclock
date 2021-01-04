package com.carlfx.worldclock;

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

public class WorldClockEvent extends Event {
    public static final EventType<WorldClockEvent> ANY  = new EventType("ANY");
    public static final EventType<WorldClockEvent> CONFIG_SHOWING  = new EventType("CONFIG_SHOWING");
    public static final EventType<WorldClockEvent> CONFIG_SHOWN  = new EventType("CONFIG_SHOWN");
    public static final EventType<WorldClockEvent> CONFIG_HIDING  = new EventType("CONFIG_HIDING");
    public static final EventType<WorldClockEvent> CONFIG_HIDDEN  = new EventType("CONFIG_HIDDEN");
    public static final EventType<WorldClockEvent> MAIN_APP_CLOSE  = new EventType("MAIN_APP_CLOSE");
    public Object payload;

    public <T> WorldClockEvent(EventType<? extends Event> eventType, T payload) {
        super(eventType);
        this.payload = payload;
    }
    public <T> WorldClockEvent(EventType<? extends Event> eventType) {
        this(eventType, null);
    }
    public <T> T getPayload() {
        return (T) payload;
    }
    public static void trigger(Node node, WorldClockEvent worldClockEvent) {
        node.getScene().getRoot().fireEvent(worldClockEvent);
    }
}