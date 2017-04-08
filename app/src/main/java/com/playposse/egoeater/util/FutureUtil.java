package com.playposse.egoeater.util;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A collection of utility methods for dealing with concurrency.
 */
public final class FutureUtil {

    public static void waitForAll(List<Future<Void>> futures)
            throws ExecutionException, InterruptedException {

        for (Future<Void> future : futures) {
            future.get();
        }
    }
}
