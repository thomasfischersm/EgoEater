package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.QueryKeys;
import com.googlecode.objectify.cmd.QueryResultIterable;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.IntermediateMatching;
import com.playposse.egoeater.backend.schema.IntermediateUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.schema.Rating;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;
import static com.playposse.egoeater.backend.util.RefUtil.createUserRef;

/**
 * A special user action that removes all test data. Test data is defined as data belonging to a
 * user with a *@tfbnw.net e-mail address.
 */
public class WipeTestDataServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(WipeTestDataServerAction.class.getName());

    private static final long SECRET = 8606434251808157605L;

    public static void wipeTestData(long secret) throws BadRequestException {
        if (SECRET != secret) {
            throw new BadRequestException("Be nice to daddy!");
        }

        List<EgoEaterUser> testUsers = findTestUsers();
        if (testUsers.size() == 0) {
            log.info("There are no test users to delete.");
            return;
        }

        List<Ref<EgoEaterUser>> refs = createUserRefs(testUsers);
        deleteMatches(refs);
        deleteRanking(refs);
        deleteRating(refs);
        deleteUsers(refs);
        deleteConversations(refs);
        deleteIntermediateMatchings(refs);
        deleteIntermediateUsers(refs);
    }

    private static void deleteMatches(List<Ref<EgoEaterUser>> refs) {
        QueryKeys<Match> keysA = ofy().load()
                .type(Match.class)
                .filter("userARef IN", refs)
                .keys();
        delete(Match.class, keysA);

        QueryKeys<Match> keysB = ofy().load()
                .type(Match.class)
                .filter("userBRef IN", refs)
                .keys();
        delete(Match.class, keysB);
    }

    private static List<Ref<EgoEaterUser>> createUserRefs(List<EgoEaterUser> testUsers) {
        List<Ref<EgoEaterUser>> refs = new ArrayList<>();
        for (EgoEaterUser user : testUsers) {
            refs.add(createUserRef(user.getId()));
        }
        return refs;
    }

    private static List<EgoEaterUser> findTestUsers() {
        List<EgoEaterUser> testUsers = new ArrayList<>();
        QueryResultIterable<EgoEaterUser> iterable = ofy().load()
                .type(EgoEaterUser.class)
                .iterable();
        for (EgoEaterUser user : iterable) {
            if (user.getEmail().endsWith("@tfbnw.net")) {
                testUsers.add(user);
            }
        }
        return testUsers;
    }

    private static void deleteRanking(List<Ref<EgoEaterUser>> refs) {
        QueryKeys<Ranking> keysA = ofy().load()
                .type(Ranking.class)
                .filter("profileId IN", refs)
                .keys();
        delete(Ranking.class, keysA);

        QueryKeys<Ranking> keysB = ofy().load()
                .type(Ranking.class)
                .filter("ratedProfileId IN", refs)
                .keys();
        delete(Ranking.class, keysB);
    }

    private static void deleteRating(List<Ref<EgoEaterUser>> refs) {
        QueryKeys<Rating> keysA = ofy().load()
                .type(Rating.class)
                .filter("rater IN", refs)
                .keys();
        delete(Rating.class, keysA);

        QueryKeys<Rating> keysB = ofy().load()
                .type(Rating.class)
                .filter("winner IN", refs)
                .keys();
        delete(Rating.class, keysB);

        QueryKeys<Rating> keysC = ofy().load()
                .type(Rating.class)
                .filter("loser IN", refs)
                .keys();
        delete(Rating.class, keysC);
    }

    private static void deleteConversations(List<Ref<EgoEaterUser>> refs) {
        QueryKeys<Conversation> keysA = ofy().load()
                .type(Conversation.class)
                .filter("profileRefA IN", refs)
                .keys();
        delete(Conversation.class, keysA);

        QueryKeys<Conversation> keysB = ofy().load()
                .type(Conversation.class)
                .filter("profileRefB IN", refs)
                .keys();
        delete(Conversation.class, keysB);
    }

    private static void deleteIntermediateMatchings(List<Ref<EgoEaterUser>> refs) {
        QueryKeys<IntermediateMatching> keysA = ofy().load()
                .type(IntermediateMatching.class)
                .filter("profileId IN", refs)
                .keys();
        delete(IntermediateMatching.class, keysA);

        QueryKeys<IntermediateMatching> keysB = ofy().load()
                .type(IntermediateMatching.class)
                .filter("ratedProfileId IN", refs)
                .keys();
        delete(IntermediateMatching.class, keysB);
    }

    private static void deleteIntermediateUsers(List<Ref<EgoEaterUser>> refs) {
        ofy().delete()
                .type(IntermediateUser.class).ids(ToLongs(refs));
    }

    private static void deleteUsers(List<Ref<EgoEaterUser>> refs) {
        ofy().delete()
                .type(EgoEaterUser.class).ids(ToLongs(refs));
        log.info("Deleted " + refs.size() + " EgoEaterUser");
    }

    private static <A> List<Long> ToLongs(List<Ref<A>> refs) {
        List<Long> longs = new ArrayList<>();
        for (Ref<A> ref : refs) {
            longs.add(ref.getKey().getId());
        }
        return longs;
    }

    private static <A> List<Long> ToLongs(QueryKeys<A> keys) {
        List<Long> longs = new ArrayList<>();
        for (Key<A> key : keys) {
            longs.add(key.getId());
        }
        return longs;
    }

    private static <A> void delete(Class<?> type, QueryKeys<A> keys) {
        List<Long> ids = ToLongs(keys);
        ofy().delete()
                .type(type)
                .ids(ids);
        log.info("Deleted " + ids.size() + " " + type.getSimpleName());
    }
}
