package com.playposse.egoeater.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for dealing with collections.
 */
public final class CollectionsUtil {

    private CollectionsUtil() {}

    /**
     * Combines lists until the a specified amount of items has been reached.
     */
    public static <D> List<D> combine(int limit, List<D>... lists) {
        List<D> result = new ArrayList<>();
        int counter = 0;
        outer: for (List<D> list : lists) {
            if (list != null) {
                for (D value : list) {
                    if (counter >= limit) {
                        break outer;
                    }

                    counter++;
                    result.add(value);
                }
            }
        }

        return result;
    }

    public static <T> T[] concatenate (T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen+bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
