package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * A record of a user's choice between two profiles.
 */
@Entity
@Cache
public class Rating {

    @Id
    private Long id;
    @Index
    private Ref<EgoEaterUser> rater;
    @Index
    private Ref<EgoEaterUser> winner;
    @Index
    private Ref<EgoEaterUser> loser;
    private long created = System.currentTimeMillis();

    public Rating() {
    }

    public Rating(long raterId, long winnerId, long loserId) {
        rater = Ref.create(Key.create(EgoEaterUser.class, raterId));
        winner = Ref.create(Key.create(EgoEaterUser.class, winnerId));
        loser = Ref.create(Key.create(EgoEaterUser.class, loserId));
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getRater() {
        return rater;
    }

    public Ref<EgoEaterUser> getWinner() {
        return winner;
    }

    public Ref<EgoEaterUser> getLoser() {
        return loser;
    }

    public long getCreated() {
        return created;
    }
}
