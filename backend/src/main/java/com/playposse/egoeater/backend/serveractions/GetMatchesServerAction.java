package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.beans.MatchBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.util.RefUtil;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that returns the matches of a user.
 */
public class GetMatchesServerAction extends AbstractServerAction {

    public static List<MatchBean> getMatches(long sessionId) throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);
        Ref<EgoEaterUser> userRef = RefUtil.createUserRef(egoEaterUser);

        // Load matches.
        List<Match> matches = ofy().load()
                .type(Match.class)
                .filter("userARef =", userRef)
                .list();

        // Create response.
        List<MatchBean> matchBeans = new ArrayList<>(matches.size());
        for (Match match : matches) {
            matchBeans.add(new MatchBean(match, egoEaterUser));
        }

        return matchBeans;
    }
}
