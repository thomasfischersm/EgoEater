package com.playposse.egoeater.backend.beans.admin;

import com.playposse.egoeater.backend.schema.EgoEaterUser;

/**
 * A transport bean to export EgoEaterUser data.
 */
public class AdminEgoEaterUserBean {
    
    private Long id;
    private String fbProfileId;
    private Long lastLogin;
    private Long created;
    private boolean isActive;
    private String firstName;
    private String lastName;
    private String email;
    private String profileText;
    private double latitude;
    private double longitude;
    private String city;
    private String state;
    private String country;
    // FB stores the birthday as mm/dd/yyyy, yyyy, or mm/dd depending on privacy settings.
    private String birthday;
    // FB stores this as 'male', 'female', or a custom value.
    private String gender;
    private String profilePhoto0;
    private String profilePhoto1;
    private String profilePhoto2;

    public AdminEgoEaterUserBean() {
    }
    
    public AdminEgoEaterUserBean(EgoEaterUser egoEaterUser) {
        id = egoEaterUser.getId();
        fbProfileId = egoEaterUser.getFbProfileId();
        lastLogin = egoEaterUser.getLastLogin();
        created = egoEaterUser.getCreated();
        isActive = egoEaterUser.isActive();
        firstName = egoEaterUser.getFirstName();
        lastName = egoEaterUser.getLastName();
        email = egoEaterUser.getEmail();
        profileText = egoEaterUser.getProfileText();
        latitude = egoEaterUser.getLatitude();
        longitude = egoEaterUser.getLongitude();
        city = egoEaterUser.getCity();
        state = egoEaterUser.getState();
        country = egoEaterUser.getCountry();
        birthday = egoEaterUser.getBirthday();
        gender = egoEaterUser.getGender();

        if (egoEaterUser.getProfilePhotos() != null) {
            if (egoEaterUser.getProfilePhotos().size() > 0) {
                profilePhoto0 = egoEaterUser.getProfilePhotos().get(0).getUrl();
            }
            if (egoEaterUser.getProfilePhotos().size() > 1) {
                profilePhoto1 = egoEaterUser.getProfilePhotos().get(1).getUrl();
            }
            if (egoEaterUser.getProfilePhotos().size() > 2) {
                profilePhoto2 = egoEaterUser.getProfilePhotos().get(2).getUrl();
            }
        }
    }

    public Long getId() {
        return id;
    }

    public String getFbProfileId() {
        return fbProfileId;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public Long getCreated() {
        return created;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
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

    public String getGender() {
        return gender;
    }

    public String getProfilePhoto0() {
        return profilePhoto0;
    }

    public String getProfilePhoto1() {
        return profilePhoto1;
    }

    public String getProfilePhoto2() {
        return profilePhoto2;
    }
}
