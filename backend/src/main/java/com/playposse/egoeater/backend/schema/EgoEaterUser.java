package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * Objectify entity that represents a user.
 */
@Entity
@Cache
public class EgoEaterUser {

    @Id
    private Long id;
    @Index
    private String fbProfileId;
    @Index
    private Long sessionId = null;
    private String firebaseToken;
    private Long lastLogin = System.currentTimeMillis();
    private Long created = System.currentTimeMillis();
    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private String profileText;
    // TODO: Add GPS
    // TODO: Add photos
    // TODO: Add age
    private List<ProfilePhoto> profilePhotos = new ArrayList<>();

    public EgoEaterUser() {
    }

    public EgoEaterUser(
            String fbProfileId,
            Long sessionId,
            String firebaseToken,
            String firstName,
            String lastName,
            String name,
            String email) {

        this.fbProfileId = fbProfileId;
        this.sessionId = sessionId;
        this.firebaseToken = firebaseToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = name;
        this.email = email;

        created = System.currentTimeMillis();
        lastLogin = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public String getFbProfileId() {
        return fbProfileId;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public String getFirebaseToken() {
        return firebaseToken;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public Long getCreated() {
        return created;
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

    public String getEmail() {
        return email;
    }

    public String getProfileText() {
        return profileText;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public void setFirebaseToken(String firebaseToken) {
        this.firebaseToken = firebaseToken;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfileText(String profileText) {
        this.profileText = profileText;
    }

    public List<ProfilePhoto> getProfilePhotos() {
        return profilePhotos;
    }
}
