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
    @Index
    private double latitude;
    @Index
    private double longitude;
    private String city;
    private String state;
    private String country;
    // FB stores the birthday as mm/dd/yyyy, yyyy, or mm/dd depending on privacy settings.
    private String birthday;
    // FB stores this as 'male', 'female', or a custom value.
    private String gender;
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
            String email,
            String birthday,
            String gender) {

        this.fbProfileId = fbProfileId;
        this.sessionId = sessionId;
        this.firebaseToken = firebaseToken;
        this.firstName = firstName;
        this.lastName = lastName;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
        this.gender = gender;

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public List<ProfilePhoto> getProfilePhotos() {
        return profilePhotos;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
