package com.playposse.egoeater.backend.generatematches;

import com.playposse.egoeater.backend.schema.MatchesServletLog;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A {@link HttpServlet} that is triggered by the AppEngine CRON service on a daily basis to generate new
 * matches.
 */
public class GenerateMatchesServlet extends HttpServlet {

    private static final int STATUS_OK = 0;
    private static final String CRON_HTTP_HEADER_PARAMETER = "X-Appengine-Cron";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        // Log execution
        long end = System.currentTimeMillis();
        int duration = (int) (end - start);
        boolean isRunByCron = isRunByCron(req);
        MatchesServletLog log = new MatchesServletLog(duration, STATUS_OK, isRunByCron);
        ofy().save().entity(log).now();
    }

    private static boolean isRunByCron(HttpServletRequest req) {
        String header = req.getHeader(CRON_HTTP_HEADER_PARAMETER);
        return Boolean.TRUE.toString().equalsIgnoreCase(header);
    }
}
