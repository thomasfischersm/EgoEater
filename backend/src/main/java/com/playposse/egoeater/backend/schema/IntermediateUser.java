package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Stringify;
import com.playposse.egoeater.backend.util.LongStringifier;

import java.util.HashMap;
import java.util.Map;

/**
 * An Objectify entity that stores intermediate information for a user during the matching process.
 */
@Entity
@Cache
public class IntermediateUser {

    @Id
    private Long profileId;
    private int matchesCount = 0;
    @Stringify(LongStringifier.class)
    private Map<Long, Integer> profileIdToRankMap = new HashMap<>();

    public IntermediateUser(
            Ref<EgoEaterUser> profileId,
            int matchesCount,
            Map<Long, Integer> profileIdToRankMap) {

        this.profileId = profileId.getKey().getId();
        this.matchesCount = matchesCount;
        this.profileIdToRankMap = profileIdToRankMap;
    }

    public void setMatchesCount(int matchesCount) {
        this.matchesCount = matchesCount;
    }

    public Long getProfileId() {
        return profileId;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public Map<Long, Integer> getProfileIdToRankMap() {
        return profileIdToRankMap;
    }
}
