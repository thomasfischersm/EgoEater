package com.playposse.egoeater.util.geocoder;

import com.playposse.egoeater.util.StringUtil;

/**
 * A data class to make it easier to pass locale information around.
 */
public class Locale {

    private final String city;
    private final String state;
    private final String country;

    public Locale(String city, String state, String country) {
        this.city = city;
        this.state = state;
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getCountry() {
        return country;
    }

    public boolean hasEmptyValue() {
        return StringUtil.isEmpty(city) || StringUtil.isEmpty(state) || StringUtil.isEmpty(country);
    }
}
