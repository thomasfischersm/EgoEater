package com.playposse.egoeater.backend.beans;

import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.ProfilePhoto;
import com.playposse.egoeater.backend.util.DataMunchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A bean for transport between the app and cloud that represents a public profile.
 */
public class ProfileBean {

    private long userId;
    private boolean isActive;
    private String firstName;
    private String profileText;
    private int distance;
    private String city;
    private String state;
    private String country;
    private Integer age;
    private String gender;
    private List<String> profilePhotoUrls = new ArrayList<>();


    public ProfileBean() {
    }

    public ProfileBean(EgoEaterUser egoEaterProfile, EgoEaterUser egoEaterUser) {
        double rawDistance = DataMunchUtil.getDistance(
                egoEaterUser.getLatitude(),
                egoEaterUser.getLongitude(),
                egoEaterProfile.getLatitude(),
                egoEaterProfile.getLongitude());

        userId = egoEaterProfile.getId();
        isActive = egoEaterProfile.isActive();
        firstName = egoEaterProfile.getFirstName();
        profileText = egoEaterProfile.getProfileText();
        this.distance = (int) rawDistance;
        city = egoEaterProfile.getCity();
        state = egoEaterProfile.getState();
        country = egoEaterProfile.getCountry();
        age = DataMunchUtil.getAge(egoEaterProfile.getBirthday());
        gender = egoEaterProfile.getGender();

        for (ProfilePhoto profilePhoto : egoEaterProfile.getProfilePhotos()) {
            profilePhotoUrls.add(profilePhoto.getUrl());
        }
    }

    public long getUserId() {
        return userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getProfileText() {
        return profileText;
    }

    public int getDistance() {
        return distance;
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

    public Integer getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public List<String> getProfilePhotoUrls() {
        return profilePhotoUrls;
    }
}
