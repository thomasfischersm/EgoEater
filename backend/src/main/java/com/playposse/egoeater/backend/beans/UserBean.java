package com.playposse.egoeater.backend.beans;

import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.ProfilePhoto;

import java.util.ArrayList;
import java.util.List;

/**
 * A transport bean that carries user information.
 */
public class UserBean {

    private long userId;
    private long sessionId;
    private String fbProfileId;
    private boolean isActive;
    private boolean isAdmin;
    private String firstName;
    private String lastName;
    private String name;
    private String profileText;
    private double latitude;
    private double longitude;
    private String city;
    private String state;
    private String country;
    private String birthday;
    private String birthdayOverride;
    private String gender;
    private List<String> profilePhotoUrls = new ArrayList<>();

    public UserBean() {
    }

    public UserBean(EgoEaterUser egoEaterUser) {
        userId = egoEaterUser.getId();
        sessionId = egoEaterUser.getSessionId();
        fbProfileId = egoEaterUser.getFbProfileId();
        isActive = egoEaterUser.isActive();
        isAdmin = egoEaterUser.isAdmin();
        firstName = egoEaterUser.getFirstName();
        lastName = egoEaterUser.getLastName();
        name = egoEaterUser.getName();
        profileText = egoEaterUser.getProfileText();
        longitude = egoEaterUser.getLongitude();
        latitude = egoEaterUser.getLatitude();
        city = egoEaterUser.getCity();
        state = egoEaterUser.getState();
        country = egoEaterUser.getCountry();
        birthday = egoEaterUser.getBirthday();
        birthdayOverride = egoEaterUser.getBirthdayOverride();
        gender = egoEaterUser.getGender();

        for (ProfilePhoto profilePhoto : egoEaterUser.getProfilePhotos()) {
            profilePhotoUrls.add(profilePhoto.getUrl());
        }
    }

    public long getUserId() {
        return userId;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getFbProfileId() {
        return fbProfileId;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getName() {
        return name;
    }

    public String getProfileText() {
        return profileText;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<String> getProfilePhotoUrls() {
        return profilePhotoUrls;
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

    public String getBirthday() {
        return birthday;
    }

    public String getBirthdayOverride() {
        return birthdayOverride;
    }

    public String getGender() {
        return gender;
    }
}
