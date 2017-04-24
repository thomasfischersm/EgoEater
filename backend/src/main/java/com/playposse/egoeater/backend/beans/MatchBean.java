package com.playposse.egoeater.backend.beans;

import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Match;

/**
 * A JSON object that represents a match.
 */
public class MatchBean {

    private long matchId;
    private ProfileBean otherProfileBean;
    private boolean isLocked;

    public MatchBean() {
    }

    public MatchBean(Match match, EgoEaterUser egoEaterUser) {
        this.matchId = match.getId();
        this.otherProfileBean = new ProfileBean(match.getUserBRef().get(), egoEaterUser);
        this.isLocked = match.isLocked();
    }

    public long getMatchId() {
        return matchId;
    }

    public ProfileBean getOtherProfileBean() {
        return otherProfileBean;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
