package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An objectify entity to store the ranking between users.
 */
@Entity
@Cache
public class Ranking {

    @Id
    private Long id;
    @Index
    private Long profileId;
    @Index
    private Long ratedProfileId;
    private int wins = 0;
    private int losses = 0;
    private int winsLossesSum = 0;

    public Ranking() {
    }

    public Ranking(Long profileId, Long ratedProfileId) {
        this.profileId = profileId;
        this.ratedProfileId = ratedProfileId;
    }

    public void registerWin() {
        wins++;
        winsLossesSum++;
    }

    public void registerLoss() {
        losses++;
        winsLossesSum--;
    }

    public Long getId() {
        return id;
    }

    public Long getProfileId() {
        return profileId;
    }

    public Long getRatedProfileId() {
        return ratedProfileId;
    }

    public int getWins() {
        return wins;
    }

    public int getLosses() {
        return losses;
    }

    public int getWinsLossesSum() {
        return winsLossesSum;
    }
}
