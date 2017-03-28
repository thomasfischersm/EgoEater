package com.playposse.egoeater.util;

import android.support.annotation.Nullable;

/**
 * Helpful methods for dealing with strings.
 */
public class StringUtil {

    public static boolean isEmpty(@Nullable String str) {
        return (str == null) || (str.trim().length() == 0);
    }
}
