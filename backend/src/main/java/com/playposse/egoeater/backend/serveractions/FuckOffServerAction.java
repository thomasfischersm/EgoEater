package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.firebase.NotifyUnmatchFirebaseServerAction;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.FuckOffLog;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.util.RefUtil;

import java.io.IOException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action where one user unmatches another user.
 */
public class FuckOffServerAction extends AbstractServerAction {

    public static void fuckOff(long sessionId, long partnerId) throws BadRequestException, IOException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);
        Long userId = egoEaterUser.getId();
        EgoEaterUser partnerUser = loadUserById(partnerId);

        // Look up each other's rank.
        Integer rank = getRank(userId, partnerId);
        Integer rankBack = getRank(partnerId, userId);

        // Create log entry.
        FuckOffLog log = new FuckOffLog(userId, partnerId, rank, rankBack);
        ofy().save()
                .entity(log);

        // Delete matches.
        List<Key<Match>> matchIds = getMatchIds(userId, partnerId);
        ofy().delete()
                .keys(matchIds);

        // Delete rankings.
        List<Key<Ranking>> rankingIds = getRankingIds(userId, partnerId);
        ofy().delete()
                .keys(rankingIds);

        // Add to users fuck off list.
        egoEaterUser.getFuckOffList().add(partnerId);
        ofy().save()
                .entity(egoEaterUser);
        partnerUser.getPissedOffList().add(userId);
        ofy().save()
                .entity(partnerUser);

        // Notify the other user via Firebase.
        NotifyUnmatchFirebaseServerAction.notifyUnmatch(partnerUser.getFirebaseToken(), userId);
    }

    private static List<Key<Ranking>> getRankingIds(long userId, long partnerId) {
        Ref<EgoEaterUser> userRef = RefUtil.createUserRef(userId);
        Ref<EgoEaterUser> partnerRef = RefUtil.createUserRef(partnerId);

        List<Key<Ranking>> rankingIds = ofy().load()
                .type(Ranking.class)
                .filter("profileId =", userRef)
                .filter("ratedProfileId =", partnerRef)
                .keys()
                .list();

        rankingIds.addAll(ofy().load()
                .type(Ranking.class)
                .filter("profileId =", partnerRef)
                .filter("ratedProfileId =", userRef)
                .keys()
                .list());

        return rankingIds;
    }
}
