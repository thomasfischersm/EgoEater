package com.playposse.egoeater.backend.serveractions;

import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.User;

import java.util.List;
import java.util.Random;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server that signs in the user. The first call will import the user's profile from FB.
 */
public class SignInServerAction {

    public static final Random RANDOM = new Random();

    public static UserBean signIn(String fbAccessToken, String firebaseToken) {
        long sessionId = RANDOM.nextLong();

        // Retrieve data from FB
        User fbUser = fetchUserFromFaceBook(fbAccessToken);
        String fbProfileId = fbUser.getId();

        // Check for existing user.
        List<EgoEaterUser> egoEaterUsers =
                ofy()
                        .load()
                        .type(EgoEaterUser.class)
                        .filter("fbProfileId", fbProfileId)
                        .list();
        boolean existingUser = egoEaterUsers.size() > 0;

        if (existingUser) {
            return signInExistingUser(
                    sessionId,
                    fbUser,
                    fbProfileId,
                    firebaseToken,
                    egoEaterUsers.get(0));
        } else {
            return signInNewUser(sessionId, fbUser, fbProfileId, firebaseToken);
        }
    }

    private static UserBean signInNewUser(
            long sessionId,
            User fbUser,
            String fbProfileId,
            String firebaseToken) {

        EgoEaterUser egoEaterUser = new EgoEaterUser(
                fbProfileId,
                sessionId,
                firebaseToken,
                fbUser.getFirstName(),
                fbUser.getLastName(),
                fbUser.getName(),
                fbUser.getEmail(),
                fbUser.getBirthday(),
                fbUser.getGender());

        ofy().save().entity(egoEaterUser).now();

        return new UserBean(egoEaterUser);
    }

    private static UserBean signInExistingUser(
            long sessionId,
            User fbUser,
            String fbProfileId,
            String firebaseToken,
            EgoEaterUser egoEaterUser) {

        // Update with the latest information from Facebook.
        egoEaterUser.setSessionId(sessionId);
        egoEaterUser.setFirebaseToken(firebaseToken);
        egoEaterUser.setFirstName(fbUser.getFirstName());
        egoEaterUser.setLastName(fbUser.getLastName());
        egoEaterUser.setName(fbUser.getName());
        egoEaterUser.setEmail(fbUser.getEmail());
        egoEaterUser.setBirthday(fbUser.getBirthday());
        egoEaterUser.setGender(fbUser.getGender());

        ofy().save().entity(egoEaterUser).now();

        return new UserBean(egoEaterUser);
    }

    private static User fetchUserFromFaceBook(String accessToken) {
        FacebookClient facebookClient = new DefaultFacebookClient(accessToken, Version.VERSION_2_8);
        return facebookClient.fetchObject(
                "me",
                User.class,
                Parameter.with(
                        "fields",
                        "id,name,link,first_name,last_name,email,birthday,gender"));
        // TODO: Only allow 'verified' FB users into the system to avoid fake profiles.
        // TODO: If the gender is not 'male' or 'female' tell tell the user that the system doesn't
        // work for them.
    }
}
