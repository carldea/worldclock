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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Class represents a US location.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class USLocation extends Location {
    private String state = "";
    private String zip;
    public USLocation(){}
    public USLocation(String timezone, String city, String stateCode) {
        this(timezone, city, stateCode, 0, TEMP_STD.CELSIUS);
    }
    public USLocation(String timezone, String city, String stateCode, float temp, TEMP_STD tempType) {
        super(timezone, city, "US", temp, tempType);
        setState(stateCode);
    }
    @Override
    public String getFullLocationName() {
        return getCity() + ", " + getState() + " " + getCountryCode();
    }
    @Override
    public String getCountry() {
        return "USA";
    }
    @Override
    public String getCountryCode() {
        return "US";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
