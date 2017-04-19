package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An Objectify entity that stores intermediate information for matching.
 */
@Entity
@Cache
public class IntermediateMatching {

    @Id
    private Long id;
    private Ref<EgoEaterUser> profileId;
    private Ref<EgoEaterUser> ratedProfileId;
    @Index
    private Integer rank;
    private Integer rankBack;
    @Index
    private Double matchScore;

    public IntermediateMatching() {
    }

    public IntermediateMatching(
            Ref<EgoEaterUser> profileId,
            Ref<EgoEaterUser> ratedProfileId,
            Integer rank) {

        this.profileId = profileId;
        this.ratedProfileId = ratedProfileId;
        this.rank = rank;
    }

    public void setRankBack(Integer rankBack) {
        this.rankBack = rankBack;
    }

    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getProfileId() {
        return profileId;
    }

    public Ref<EgoEaterUser> getRatedProfileId() {
        return ratedProfileId;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getRankBack() {
        return rankBack;
    }

    public Double getMatchScore() {
        return matchScore;
    }
}
