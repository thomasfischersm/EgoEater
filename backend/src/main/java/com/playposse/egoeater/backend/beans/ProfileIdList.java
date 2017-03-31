package com.playposse.egoeater.backend.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * A transport bean that wraps a list of profile ids.
 */
public class ProfileIdList {

    private List<Long> profileIds;

    public ProfileIdList(List<Long> profileIds) {
        this.profileIds = profileIds;
    }

    public List<Long> getProfileIds() {
        return profileIds;
    }
}
