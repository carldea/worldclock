package com.carlfx.worldclock;

public class USLocation extends Location {
    private String state;
    private String zip;

    public USLocation(String timezone, String city, String countryCode, float temp, TEMP_STD tempType) {
        super(timezone, city, countryCode, temp, tempType);
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
