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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A controller for the clock-widget.fxml file.
 */
public class WorldClockController {

    @FXML
    private Arc minuteHandArc;
    @FXML
    private Circle minuteHandTip;

    @FXML
    private Arc hourHandArc;
    @FXML
    private Circle hourHandTip;

    @FXML
    private Text clockTime;

    @FXML
    private Label locationRegion;
    @FXML
    private Label day;
    @FXML
    private Label monthDate;

    @FXML
    private Text temperatureText;

    @FXML
    private ImageView weatherIconImageView;

    private long startTime;
    private Tooltip weatherToolTip;

    /**
     * Start Angle of arc to draw the minute hand (start).
     */
    private Function<Integer, Integer> startAngleMinute = ( minutes ) -> {
        // 360 / 60 = 6 degrees for each minute tick on the clock
        int degrees = (60 - minutes) * 6;

        // add 90 degress to position start at the 12'o clock position.
        // JavaFX arc goes counter clockwise starting zero degrees at the 3 o'clock
        return degrees + 90;
    };

    /**
     * Extent angle of the arc to draw the minute hand (end)
     */
    private Function<Integer, Integer> extentAngleMinute = ( minutes ) -> {
        // 360 / 60 = 6 degrees for each minute tick on the clock
        int degrees = (60 - minutes) * 6;

        // make the extent angle counter clockwise to the 12'o clock position
        return 360 - degrees;
    };
    /**
     * Start Angle of arc to draw the hour hand (start).
     */
    private Function<Integer, Integer> startAngleHour = ( hours ) -> {
        // 360 / 12 = 30 degrees for each hours tick on the clock
        int degrees = (12 - hours) * 30;

        // add 90 degress to position start at the 12'o clock position.
        // JavaFX arc goes counter clockwise starting zero degrees at the 3 o'clock
        return (degrees + 90) % 360;
    };

    /**
     * Extent angle of the arc to draw the hour hand (end)
     */
    private Function<Integer, Integer> extentAngleHour = ( hours ) -> {
        // 360 / 12 = 30 degrees for each hours tick on the clock
        int degrees = (12 - hours) * 30;

        // make the extent angle counter clockwise to the 12'o clock position
        return (360 - degrees) % 360;
    };

    /**
     * Positions the ball or tip at the start of the arc
     * The angle in degrees creating a point on the unit circle multiplied by the radius.
     */
    private BiFunction<Integer, Double, double[]> tipPointXY = ( angDegrees, radius ) -> {
        double [] pointXY = new double[2];
        pointXY[0] = Math.cos(Math.toRadians(angDegrees)) * radius;
        pointXY[1] = Math.sin(Math.toRadians(angDegrees)) * radius;
        return pointXY;
    };

    SimpleLongProperty epochTime = new SimpleLongProperty(new Date().getTime());
    Timeline timeline = null;
    ChangeListener<? super Number> secondListener;

    /**
     * The main Application will have listeners when a clock is removed. To clean up the timers and listeners
     * will be stopped and removed respectively.
     */
    public void cleanup() {
        if (timeline != null) {
            timeline.stop();
            if (secondListener != null) {
                epochTime.removeListener(secondListener);
            }
        }
    }

    public void init(Location location) {
        locationRegion.setText("");
        // each tick update epoch property
        timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), /* every second */
                        actionEvent -> epochTime.set(System.currentTimeMillis())) /* update epoch */
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        BiConsumer<String, Throwable> updateWeatherUI =  (dayForecastJson, err) -> {
            try {
                // Parse weather JSON to obtain icon and weather temp info.
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> dayForecast = mapper.readValue(dayForecastJson, Map.class);
                List<Map<String, Object>> weatherInfo = (List<Map<String, Object>>) dayForecast.get("weather");
                Map<String, Object> weatherIconInfo = weatherInfo.size() > 0 ? weatherInfo.get(0) : null;
                Map<String, Object> tempInfo = (Map<String, Object>) dayForecast.get("main");
                // Load weather icon asynchronously
                Image weatherIcon = new Image("https://openweathermap.org/img/wn/%s@2x.png".formatted(weatherIconInfo.get("icon")), true);
                weatherIconImageView.setImage(weatherIcon);

                // Apply Tooltip
                if (weatherToolTip != null) {
                    Tooltip.uninstall(weatherIconImageView, weatherToolTip);
                }
                weatherToolTip = new Tooltip(weatherIconInfo.get("description").toString());
                Tooltip.install(weatherIconImageView, weatherToolTip);
                // Apply Text of temp in celsius
                String tempType = location.getTempType() == Location.TEMP_STD.CELSIUS || location.getTempType() == null ? "°C" : "°F";
                String tempText = "%d%s".formatted( Math.round(Float.parseFloat(tempInfo.get("temp").toString())), tempType);
                temperatureText.setText(tempText);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        };
        CompletableFuture<String> firstWeatherJsonFetch = getWeatherOutlook(location);
        firstWeatherJsonFetch.whenComplete(updateWeatherUI);

        // Every second tick this listener will update the ui.
        secondListener = (obs, ov, nv) -> {
            // Uncomment to test whether each clock's timeline and listeners are cleaned up.
            //System.out.println("tick tock " + location.getFullLocationName());
            String gmtOffset = location.getTimezone();
            if (gmtOffset.indexOf("GMT") > -1) {
                gmtOffset = gmtOffset.substring(3);
            }
            if ("".equals(gmtOffset.trim())) {
                gmtOffset = "0";
            }
            gmtOffset = "GMT%+d".formatted(Integer.parseInt(gmtOffset));
            Calendar newCalendar = Calendar.getInstance(TimeZone.getTimeZone(gmtOffset));

            newCalendar.setTime(new Date(nv.longValue()));
            SimpleDateFormat timeDisplay = new SimpleDateFormat("h:mm");

            timeDisplay.setTimeZone(TimeZone.getTimeZone(gmtOffset));
//            System.out.println(gmtOffset + " " +
//                    location.getFullLocationName() + " " +
//                    newCalendar.getTimeZone() +
//                    " time " + timeDisplay.format(newCalendar.getTime()));

            int hour = newCalendar.get(Calendar.HOUR) > 12 ? newCalendar.get(Calendar.HOUR)-12 : newCalendar.get(Calendar.HOUR); // 0-23
            hour = hour == 0 ? 12 : hour;
            int minute = newCalendar.get(Calendar.MINUTE);

            clockTime.setText(timeDisplay.format(newCalendar.getTime()));
            // draw blue glowing minute hand arc
            int minuteStartAngle = startAngleMinute.apply(minute);
            int minuteExtentAngle = extentAngleMinute.apply(minute);

            minuteHandArc.setStartAngle(minuteStartAngle);
            minuteHandArc.setLength(minuteExtentAngle);

            // draw blue glowing minute hand tip
            double [] minuteTipPoint = tipPointXY.apply(minuteStartAngle , 45.0);
            minuteHandTip.setTranslateX(minuteTipPoint[0]);
            minuteHandTip.setTranslateY(minuteTipPoint[1] * -1);

            // draw orange glowing hour hand arc
            int hourStartAngle = startAngleHour.apply(hour);
            int hourExtentAngle = extentAngleHour.apply(hour);

            hourHandArc.setStartAngle(hourStartAngle);
            hourHandArc.setLength(hourExtentAngle);

            // draw orange glowing hour hand tip
            double [] hourTipPoint = tipPointXY.apply(hourStartAngle , 35.0);
            hourHandTip.setTranslateX(hourTipPoint[0]);
            hourHandTip.setTranslateY(hourTipPoint[1] * -1);


            locationRegion.setText(location.getCity() + " " + location.getCountryCode());

            DateTimeFormatter dayLongformat = DateTimeFormatter.ofPattern("EEEE");
            String dayString = dayLongformat.format(ZonedDateTime.now(ZoneId.of(location.getTimezone()) ) );
            day.setText(dayString);

            DateTimeFormatter monthLongformat = DateTimeFormatter.ofPattern("MMMM d");
            String monthDateString = monthLongformat.format(ZonedDateTime.now(ZoneId.of(location.getTimezone()) ) );
            monthDate.setText(monthDateString);



            // has 10 minutes elapsed yet, if so update weather icon and temp, else continue timer.
            if (hasTimeElapsed(java.time.Duration.ofMinutes(10))) {
                // TODO Config needs to allow user to choose metric or standard
                // fetch the temp
                CompletableFuture<String> weatherJsonFetch = getWeatherOutlook(location);
                weatherJsonFetch.whenComplete(updateWeatherUI);
            }

        };
        epochTime.addListener(secondListener);

    }

    /**
     * Fetch JSON from openweathermap.org. having a three second timeout.
     * @param uri
     * @return
     */
    public CompletableFuture<String> fetch(String uri) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(java.time.Duration.ofMillis(3000))
                .build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }

    /**
     * An async fetch to get current weather as a JSON string.
     * @param location Location containing the latitude and longitude.
     * @return
     */
    private CompletableFuture<String> getWeatherOutlook(Location location) {
        if (location.getLatLong() == null || (location.getLongitude() == 0f) && location.getLatitude() == 0f) {
            return CompletableFuture.failedFuture(new Throwable("No lat long defined"));
        }

        double lat = location.getLatitude();
        double lon = location.getLongitude();
        DecimalFormat df = new DecimalFormat("###.######");
        try {
            String appId = new String(Files.readAllBytes(Paths.get(getClass().getResource("openweathermap-appid.txt").toURI())));
            if (appId == null) {
                return CompletableFuture.failedFuture(new Throwable("Error. No API token (appid) set a file called openweathermap-appid.txt."));
            }
            return fetch("https://api.openweathermap.org/data/2.5/weather?units=metric&lat=%s&lon=%s&appid=%s".formatted(df.format(lat), df.format(lon), appId));
        } catch (IOException | URISyntaxException e) {
            return CompletableFuture.failedFuture(new Throwable("Error. No API token (appid) set a file called openweathermap-appid.txt."));
        }
    }

    /**
     * Create a simple named stop watch. For example, getting temperature once every 10 minutes.
     *
     * @param duration a length of time that has elapsed. When time has been reached the startTime is reset to zero.
     * @return
     */
    private boolean hasTimeElapsed(java.time.Duration duration) {
        long now = System.currentTimeMillis();
        if (startTime == 0) {
            startTime = now;
        }
        // elapsed so reset the timer
        if ((now - startTime) >= duration.toMillis()) {
            startTime = 0;
            return true;
        }
        return false;
    }
}
