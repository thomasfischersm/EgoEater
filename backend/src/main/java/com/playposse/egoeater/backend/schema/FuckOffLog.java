package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * An Objectify entity that records that a fuck off was given.
 */
@Entity
public class FuckOffLog {

    @Id
    private Long id;
    private Long profileId;
    private Long parnerId;
    private Integer rank;
    private Integer rankBack;
    private Long created;

    public FuckOffLog() {
    }

    public FuckOffLog(Long profileId, Long parnerId, Integer rank, Integer rankBack) {
        this.profileId = profileId;
        this.parnerId = parnerId;
        this.rank = rank;
        this.rankBack = rankBack;

        created = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getParnerId() {
        return parnerId;
    }

    public Integer getRank() {
        return rank;
    }

    public Integer getRankBack() {
        return rankBack;
    }

    public Long getCreated() {
        return created;
    }
}
