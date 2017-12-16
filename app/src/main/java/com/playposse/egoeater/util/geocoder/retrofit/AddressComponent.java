package com.playposse.egoeater.util.geocoder.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A Retrofit data class that contains the address components.
 */
public class AddressComponent {

    public static final String STREET_NUMBER_TYPE = "street_number";
    public static final String ROUTE_TYPE = "route";
    public static final String NEIGHBORHOOD_TYPE = "neighborhood";
    public static final String POLITICAL_TYPE = "political";
    public static final String LOCALITY_TYPE = "locality";
    public static final String ADMINISTRATIVE_AREA_LEVEL_1_TYPE = "administrative_area_level_1";
    public static final String ADMINISTRATIVE_AREA_LEVEL_2_TYPE = "administrative_area_level_2";
    public static final String COUNTRY_TYPE = "country";
    public static final String POSTAL_CODE_TYPE = "postal_code";
    public static final String POSTAL_CODE_SUFFIX_TYPE = "postal_code_suffix";

    @SerializedName("long_name")
    private String longName;

    @SerializedName("short_name")
    private String shortName;

    private List<String> types;

    public String getLongName() {
        return longName;
    }

    public String getShortName() {
        return shortName;
    }

    public List<String> getTypes() {
        return types;
    }
}
