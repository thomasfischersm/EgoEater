package com.playposse.egoeater.backend.util;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

/**
 * A utility to make creating Objectify references more concise.
 */
public final class RefUtil {

    private RefUtil() {}

    public static Ref<EgoEaterUser> createUserRef(long profileId) {
        return Ref.create(Key.create(EgoEaterUser.class, profileId));
    }

    public static Ref<EgoEaterUser> createUserRef(EgoEaterUser user) {
        return createUserRef(user.getId());
    }
}
