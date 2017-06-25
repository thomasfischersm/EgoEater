package com.playposse.egoeater.util;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Helpful methods for dealing with strings.
 */
public class StringUtil {

    public static boolean isEmpty(@Nullable String str) {
        return (str == null) || (str.trim().length() == 0);
    }

    public static int countOccurrencesOf(String str, char c) {
        if (isEmpty(str)) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public static String concat(List<String> strs, String separator) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            if (sb.length() > 0) {
                sb.append(separator);
            }
            sb.append(str);
        }
        return sb.toString();
    }
}
