package com.playposse.egoeater.util;

import android.util.Log;

import com.restfb.types.TestUser;

/**
 * Collection of information about a fake user.
 */
public class FakeUser {

    private static final String LOG_TAG = FakeUser.class.getSimpleName();

    private String fBUserId;
    private String name;
    private String email;
    private String address;
    private int profileResId;
    private String gender;
    private String dob;
    private String fbAccessToken;
    private Long sessionId;

    public FakeUser(
            String fBUserId,
            String name,
            String email,
            String address,
            int profileResId,
            String gender,
            String dob) {

        this.fBUserId = fBUserId;
        this.name = name;
        this.email = email;
        this.address = address;
        this.profileResId = profileResId;
        this.gender = gender;
        this.dob = dob;
    }

    public String getfBUserId() {
        return fBUserId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public int getProfileResId() {
        return profileResId;
    }

    public String getGender() {
        return gender;
    }

    public String getDob() {
        return dob;
    }

    public String getFbAccessToken() {
        return fbAccessToken;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setFbAccessToken(String fbAccessToken) {
        this.fbAccessToken = fbAccessToken;
    }

    public void setTestUser(TestUser testUser) {
        this.fBUserId = testUser.getId();
        this.fbAccessToken = testUser.getAccessToken();
        this.email = testUser.getEmail();

        Log.i(LOG_TAG, "setTestUser: Created Facebook test user with e-mail: " + email);
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}
