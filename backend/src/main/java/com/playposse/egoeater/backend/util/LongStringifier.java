package com.playposse.egoeater.backend.util;

import com.googlecode.objectify.stringifier.Stringifier;

/**
 * An Objectify {@link Stringifier} for {@link Long} values.
 */
public class LongStringifier implements Stringifier<Long> {

    @Override
    public String toString(Long obj) {
        return obj.toString();
    }

    @Override
    public Long fromString(String str) {
        return Long.valueOf(str);
    }
}
