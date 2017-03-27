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
    private String firstName;
    private String lastName;
    private String name;
    private String profileText;
    private List<String> profilePhotoUrls = new ArrayList<>();


    public UserBean() {
    }

    public UserBean(EgoEaterUser egoEaterUser) {
        userId = egoEaterUser.getId();
        sessionId = egoEaterUser.getSessionId();
        fbProfileId = egoEaterUser.getFbProfileId();
        firstName = egoEaterUser.getFirstName();
        lastName = egoEaterUser.getLastName();
        name = egoEaterUser.getName();
        profileText = egoEaterUser.getProfileText();

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

    public List<String> getProfilePhotoUrls() {
        return profilePhotoUrls;
    }
}
