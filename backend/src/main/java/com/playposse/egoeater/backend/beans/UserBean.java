package com.playposse.egoeater.backend.beans;

import com.playposse.egoeater.backend.schema.EgoEaterUser;

/**
 * A transport bean that carries user information.
 */
public class UserBean {

    private long userId;
    private long sessionId;
    private String firstName;
    private String lastName;
    private String name;

    public UserBean() {
    }

    public UserBean(EgoEaterUser egoEaterUser) {
        userId = egoEaterUser.getId();
        sessionId = egoEaterUser.getSessionId();
        firstName = egoEaterUser.getFirstName();
        lastName = egoEaterUser.getLastName();
        name = egoEaterUser.getName();
    }

    public long getUserId() {
        return userId;
    }

    public long getSessionId() {
        return sessionId;
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
}
