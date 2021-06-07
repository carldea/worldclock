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
module worldclock {
    requires java.desktop;
    requires java.net.http;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    opens com.carlfx.worldclock to com.fasterxml.jackson.core, com.fasterxml.jackson.databind, com.fasterxml.jackson.annotation, javafx.fxml, javafx.web;
    exports com.carlfx.worldclock;
}