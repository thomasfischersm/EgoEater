package com.playposse.egoeater.backend.util;

import com.googlecode.objectify.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A concurrency utility that waits for all Objectify entities to be saved or deleted. The
 * Objectify flush() method doesn't seem to wait for in process operations to finish.
 */
public class ObjectifyWaiter {

    private static final Logger log = Logger.getLogger(ObjectifyWaiter.class.getName());

    private List<Result<?>> results = new ArrayList<>();

    public void addResult(Result<?> result) {
        results.add(result);
    }

    public void flush() {
        ofy().flush();

        long start = System.currentTimeMillis();
        log.info("Waiting for completion of Objectify operations " + results.size());

        for (Result<?> result : results) {
            result.now();
        }

        results.clear();

        long end = System.currentTimeMillis();
        log.info("Completed waiting for Objectify operations " + (end - start) + "ms");
    }
}
