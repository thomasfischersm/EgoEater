package com.playposse.egoeater.util.geocoder.retrofit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * A Retrofit data container for a single geo result.
 */
public class Result {

    @SerializedName("address_components")
    private List<AddressComponent> addressComponents;

    @SerializedName("formatted_address")
    private String formattedAddress;

    @SerializedName("place_id")
    private String placeId;

    private List<String> types;

    public List<AddressComponent> getAddressComponents() {
        return addressComponents;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getPlaceId() {
        return placeId;
    }

    public List<String> getTypes() {
        return types;
    }
}
