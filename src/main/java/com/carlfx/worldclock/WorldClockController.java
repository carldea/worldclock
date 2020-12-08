package com.carlfx.worldclock;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Function;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

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
        return degrees + 90;
    };

    /**
     * Extent angle of the arc to draw the hour hand (end)
     */
    private Function<Integer, Integer> extentAngleHour = ( hours ) -> {
        // 360 / 12 = 30 degrees for each hours tick on the clock
        int degrees = (12 - hours) * 30;

        // make the extent angle counter clockwise to the 12'o clock position
        return 360 - degrees;
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

    public void init(Location location, LongProperty epochTime) {

        epochTime.addListener( (obs, ov, nv) -> {
            Calendar newCalendar = Calendar.getInstance(TimeZone.getTimeZone(location.getTimezone()));
            newCalendar.setTime(new Date(nv.longValue()));
            SimpleDateFormat timeDisplay = new SimpleDateFormat("h:mm");
            timeDisplay.setTimeZone(TimeZone.getTimeZone(location.getTimezone()));

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

            SimpleDateFormat dayLongformat = new SimpleDateFormat("EEEE");
            dayLongformat.setTimeZone(TimeZone.getTimeZone(location.getTimezone()));
            day.setText(dayLongformat.format(newCalendar.getTime()));

            SimpleDateFormat monthLongformat = new SimpleDateFormat("MMMM d");
            monthLongformat.setTimeZone(TimeZone.getTimeZone(location.getTimezone()));
            monthDate.setText(monthLongformat.format(newCalendar.getTime()));

            String tempType = location.getTempType() == Location.TEMP_STD.CELSIUS ? "°C" : "°F";
            temperatureText.setText( (int)location.getTemperature() + tempType);

        });

    }
}
