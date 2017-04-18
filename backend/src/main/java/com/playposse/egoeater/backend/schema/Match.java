package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Objectify entity that represents a match between two users.
 */
@Entity
@Cache
public class Match {

    @Id
    private Long id;

    @Index
    private Ref<EgoEaterUser> userARef;

    @Index
    private Ref<EgoEaterUser> userBRef;

    @Index
    private boolean isLocked = false;
    private long created = System.currentTimeMillis();

    public Match(Ref<EgoEaterUser> userARef, Ref<EgoEaterUser> userBRef) {
        this.userARef = userARef;
        this.userBRef = userBRef;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getUserARef() {
        return userARef;
    }

    public Ref<EgoEaterUser> getUserBRef() {
        return userBRef;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public long getCreated() {
        return created;
    }
}
