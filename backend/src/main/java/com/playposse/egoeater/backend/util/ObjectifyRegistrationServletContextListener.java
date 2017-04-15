package com.playposse.egoeater.backend.util;

import com.googlecode.objectify.ObjectifyService;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.GeoTest;
import com.playposse.egoeater.backend.schema.MatchesServletLog;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.schema.Rating;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A {@link ServletContextListener} that registers the Objectify entities on startup.
 */
public class ObjectifyRegistrationServletContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ObjectifyService.register(EgoEaterUser.class);
        ObjectifyService.register(MatchesServletLog.class);
        ObjectifyService.register(Ranking.class);
        ObjectifyService.register(Rating.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Nothing to do.
    }
}
