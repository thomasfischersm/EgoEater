package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Stringify;
import com.playposse.egoeater.backend.util.LongStringifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<Ref<EgoEaterUser>> lockedMatchingProfiles = new ArrayList<>();

    public IntermediateUser() {
    }

    public IntermediateUser(
            Ref<EgoEaterUser> profileId,
            Map<Long, Integer> profileIdToRankMap,
            List<Ref<EgoEaterUser>> lockedMatchingProfiles) {

        this.profileId = profileId.getKey().getId();
        this.profileIdToRankMap = profileIdToRankMap;
        this.lockedMatchingProfiles = lockedMatchingProfiles;

        matchesCount = lockedMatchingProfiles.size();
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

    public List<Ref<EgoEaterUser>> getLockedMatchingProfiles() {
        return lockedMatchingProfiles;
    }
}
