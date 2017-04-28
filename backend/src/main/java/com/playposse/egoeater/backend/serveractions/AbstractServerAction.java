package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.LoadResult;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.cmd.QueryKeys;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.schema.Match;
import com.playposse.egoeater.backend.schema.ProfilePhoto;
import com.playposse.egoeater.backend.schema.Ranking;
import com.playposse.egoeater.backend.util.RefUtil;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A base class for server actions that offers useful methods.
 */
public abstract class AbstractServerAction {

    protected static final String BUCKET_NAME = "ego-eater.appspot.com";

    protected static EgoEaterUser loadUser(long sessionId) throws BadRequestException {
        List<EgoEaterUser> egoEaterUsers =
                ofy()
                        .load()
                        .type(EgoEaterUser.class)
                        .filter("sessionId", sessionId)
                        .list();

        if (egoEaterUsers.size() != 1) {
            throw new BadRequestException("The session id " + sessionId +
                    " resulted in an unexpected number of users: " + egoEaterUsers.size());
        }

        return egoEaterUsers.get(0);
    }

    protected static EgoEaterUser loadUserById(long profileId) throws BadRequestException {
        EgoEaterUser user = ofy()
                .load()
                .type(EgoEaterUser.class)
                .id(profileId).now();

        if (user == null) {
            throw new BadRequestException("A user with the profile id " + profileId
                    + " could not be found.");
        }

        return user;
    }

    protected static List<ProfilePhoto> deleteProfilePhoto(
            int photoIndex,
            EgoEaterUser egoEaterUser,
            Storage storage) {

        List<ProfilePhoto> profilePhotos = egoEaterUser.getProfilePhotos();
        if (photoIndex < profilePhotos.size()) {
            ProfilePhoto oldProfilePhoto = profilePhotos.get(photoIndex);
            deleteFile(oldProfilePhoto.getFileName(), storage);
            profilePhotos.remove(photoIndex);
        }
        return profilePhotos;
    }

    protected static void deleteFile(String fileName, Storage storage) {
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        storage.delete(blobId);
    }

    /**
     * Sorts two user refs by id ascending.
     * <p>
     * <p>This is used by queries that are joined by two users. We always assume that the user with
     * the smaller id is user A. That way, we don't have to query for both possibilities.
     */
    @SuppressWarnings("unchecked")
    protected static Ref<EgoEaterUser>[] sortUserRefs(
            Ref<EgoEaterUser> ref0,
            Ref<EgoEaterUser> ref1) {

        if (ref0.getKey().getId() < ref1.getKey().getId()) {
            return (Ref<EgoEaterUser>[]) new Ref[]{ref0, ref1};
        } else {
            return (Ref<EgoEaterUser>[]) new Ref[]{ref1, ref0};
        }
    }

    protected static Ref<EgoEaterUser>[] sortUserRefs(long profile0Id, long profile1Id) {
        return sortUserRefs(RefUtil.createUserRef(profile0Id), RefUtil.createUserRef(profile1Id));
    }

    protected static Conversation getConversationByProfileIds(long userId, long partnerId) {
        Ref<EgoEaterUser>[] userRefs = sortUserRefs(userId, partnerId);

        List<Conversation> conversations = ofy().load()
                .type(Conversation.class)
                .filter("profileRefA =", userRefs[0])
                .filter("profileRefB =", userRefs[1])
                .list();

        if ((conversations == null) || (conversations.size() == 0)) {
            return null;
        } else {
            return conversations.get(0);
        }
    }

    protected static Integer getRank(long userId, long partnerId) {
        List<Ranking> rankings = ofy().load()
                .type(Ranking.class)
                .filter("profileId =", RefUtil.createUserRef(userId))
                .filter("ratedProfileId =", RefUtil.createUserRef(partnerId))
                .list();

        if (rankings.size() == 1) {
            return rankings.get(0).getWinsLossesSum();
        } else {
            return null;
        }
    }

    protected static List<Key<Match>> getMatchIds(long userId, long partnerId) {
        Ref<EgoEaterUser>[] userRefs = sortUserRefs(userId, partnerId);

        List<Key<Match>> matches = ofy().load()
                .type(Match.class)
                .filter("userARef =", userRefs[0])
                .filter("userBRef =", userRefs[1])
                .keys()
                .list();

        // TODO: Only one side should be needed in the future!
        matches.addAll(ofy().load()
                .type(Match.class)
                .filter("userARef =", userRefs[1])
                .filter("userBRef =", userRefs[0])
                .keys()
                .list());

        return matches;
    }
}
