package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An Objectify entity to log whenever a user closes or re-opens the account.
 */
@Entity
public class DeactivationLog {

    @Id private Long id;
    @Index private Ref<EgoEaterUser> profileRef;
    private boolean newActiveState;
    private Long created;

    public DeactivationLog() {
    }

    public DeactivationLog(Ref<EgoEaterUser> profileRef, boolean newActiveState) {
        this.profileRef = profileRef;
        this.newActiveState = newActiveState;
        created = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getProfileRef() {
        return profileRef;
    }

    public boolean isNewActiveState() {
        return newActiveState;
    }

    public Long getCreated() {
        return created;
    }
}
