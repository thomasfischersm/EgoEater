package com.playposse.egoeater.util;

/**
 * A utility for dealing with integers.
 */
public final class IntegerUtil {

    private IntegerUtil() {}

    public static int get(Integer integer, int defaultValue) {
        return (integer != null) ? integer : defaultValue;
    }
}
