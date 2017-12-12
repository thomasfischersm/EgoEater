package com.playposse.egoeater.backend.servlets;

import com.googlecode.objectify.cmd.LoadType;
import com.playposse.egoeater.backend.schema.EgoEaterUser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A servlet that re-saves all the users to update the index on the isActive column.
 */
public class ReindexUsersServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ReindexUsersServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        log.info("ReindexUsersServlet starts");
        long start = System.currentTimeMillis();

        LoadType<EgoEaterUser> iterator = ofy().load()
                .type(EgoEaterUser.class);
        for (EgoEaterUser user : iterator) {
            ofy().save().entities(user);
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html");

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
        writer.write("Happiness reigns!");
        writer.flush();

        long end = System.currentTimeMillis();
        log.info("ReindexUsersServlet ends. Duration: " + (end - start) + "ms");
    }
}
