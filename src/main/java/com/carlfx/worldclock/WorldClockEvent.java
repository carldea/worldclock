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

import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;

/**
 * Custom events to message between parent and children views.
 */
public class WorldClockEvent extends Event {
    public static final EventType<WorldClockEvent> ANY  = new EventType("ANY");
    public static final EventType<WorldClockEvent> CLEANUP_CLOCK  = new EventType("CLEANUP_CLOCK");
    public static final EventType<WorldClockEvent> CONFIG_SHOWING  = new EventType("CONFIG_SHOWING");
    public static final EventType<WorldClockEvent> CONFIG_SHOWN  = new EventType("CONFIG_SHOWN");
    public static final EventType<WorldClockEvent> CONFIG_HIDING  = new EventType("CONFIG_HIDING");
    public static final EventType<WorldClockEvent> CONFIG_HIDDEN  = new EventType("CONFIG_HIDDEN");
    public static final EventType<WorldClockEvent> MAIN_APP_CLOSE  = new EventType("MAIN_APP_CLOSE");
    public static final EventType<WorldClockEvent> LOCATION_ADD  = new EventType("LOCATION_ADD");
    public static final EventType<WorldClockEvent> LOCATION_UPDATE  = new EventType("LOCATION_UPDATE");
    public static final EventType<WorldClockEvent> LOCATION_REMOVE  = new EventType("LOCATION_REMOVE");
    public static final EventType<WorldClockEvent> LOCATION_MOVE_UP  = new EventType("LOCATION_MOVE_UP");
    public static final EventType<WorldClockEvent> LOCATION_MOVE_DOWN  = new EventType("LOCATION_MOVE_DOWN");

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

    // TODO all children to be notified from parent.
    public static <T> void trigger(Node node, EventType<WorldClockEvent> eventType, T payload) {
        node.getScene().getRoot().fireEvent(new WorldClockEvent(eventType, payload));
    }

    public static void trigger(Node node, WorldClockEvent worldClockEvent) {
        node.getScene().getRoot().fireEvent(worldClockEvent);
    }
}