/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.playposse.egoeater.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.admin.AdminEgoEaterUserBean;
import com.playposse.egoeater.backend.beans.admin.AdminStatisticsBean;
import com.playposse.egoeater.backend.beans.MatchBean;
import com.playposse.egoeater.backend.beans.MaxMessageIndexResponseBean;
import com.playposse.egoeater.backend.beans.MessageBean;
import com.playposse.egoeater.backend.beans.PhotoBean;
import com.playposse.egoeater.backend.beans.ProfileBean;
import com.playposse.egoeater.backend.beans.ProfileIdList;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.serveractions.UpdateBirthdayOverrideServerAction;
import com.playposse.egoeater.backend.serveractions.admin.GetAdminDumpEgoEaterUserServerAction;
import com.playposse.egoeater.backend.serveractions.admin.GetAdminStatisticsServerAction;
import com.playposse.egoeater.backend.serveractions.UpdateAccountStatusServerAction;
import com.playposse.egoeater.backend.serveractions.DeleteProfilePhotoServerAction;
import com.playposse.egoeater.backend.serveractions.FuckOffServerAction;
import com.playposse.egoeater.backend.serveractions.GetConversationServerAction;
import com.playposse.egoeater.backend.serveractions.GetMatchesServerAction;
import com.playposse.egoeater.backend.serveractions.GetMaxMessageIndexServerAction;
import com.playposse.egoeater.backend.serveractions.GetProfilesByDistanceServerAction;
import com.playposse.egoeater.backend.serveractions.GetProfilesByIdServerAction;
import com.playposse.egoeater.backend.serveractions.ReportAbuseServerAction;
import com.playposse.egoeater.backend.serveractions.ReportMessageReadServerAction;
import com.playposse.egoeater.backend.serveractions.ReportRankingServerAction;
import com.playposse.egoeater.backend.serveractions.SaveProfileServerAction;
import com.playposse.egoeater.backend.serveractions.SendMessageServerAction;
import com.playposse.egoeater.backend.serveractions.SignInServerAction;
import com.playposse.egoeater.backend.serveractions.SwapProfilePhotosServerAction;
import com.playposse.egoeater.backend.serveractions.UpdateFirebaseTokenServerAction;
import com.playposse.egoeater.backend.serveractions.UpdateLocationServerAction;
import com.playposse.egoeater.backend.serveractions.UploadProfilePhotoServerAction;
import com.playposse.egoeater.backend.serveractions.WipeTestDataServerAction;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Named;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "egoEaterApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.egoeater.playposse.com",
                ownerName = "backend.egoeater.playposse.com",
                packagePath = ""
        )
)
public class EgoEaterEndPoint {

    private static final Logger log = Logger.getLogger(EgoEaterEndPoint.class.getName());

    @ApiMethod(name = "signIn")
    public UserBean signIn(
            @Named("fbAccessToken") String fbAccessToken,
            @Named("firebaseToken") String firebaseToken) {

        return SignInServerAction.signIn(fbAccessToken, firebaseToken);
    }

    @ApiMethod(name = "updateFireBaseToken")
    public void updateFireBaseToken(
            @Named("sessionId") long sessionId,
            @Named("firebaseToken") String firebaseToken) throws BadRequestException {

        UpdateFirebaseTokenServerAction.updateFireBaseToken(sessionId, firebaseToken);
    }

    @ApiMethod(name = "uploadProfilePhoto")
    public UserBean uploadProfilePhoto(
            @Named("sessionId") long sessionId,
            @Named("photoIndex") int photoIndex,
            PhotoBean photoBean) throws BadRequestException, IOException {

        log.info("got a photo of size " + photoBean.getBytes().length);
        return UploadProfilePhotoServerAction.uploadProfilePhoto(sessionId, photoIndex, photoBean);
    }

    @ApiMethod(name = "deleteProfilePhoto")
    public UserBean deleteProfilePhoto(
            @Named("sessionId") long sessionId,
            @Named("photoIndex") int photoIndex) throws BadRequestException, IOException {

        return DeleteProfilePhotoServerAction.deleteProfilePhoto(sessionId, photoIndex);
    }

    @ApiMethod(name = "saveProfile")
    public UserBean saveProfile(
            @Named("sessionId") long sessionId,
            @Named("profileText") String profileText) throws BadRequestException, IOException {

        return SaveProfileServerAction.saveProfile(sessionId, profileText);
    }

    @ApiMethod(name = "updateLocation")
    public UserBean updateLocation(
            @Named("sessionId") long sessionId,
            @Named("latitude") double latitude,
            @Named("longitude") double longitude,
            @Nullable @Named("city") String city,
            @Nullable @Named("state") String state,
            @Named("country") String country) throws BadRequestException, IOException {

        return UpdateLocationServerAction.updateLocation(
                sessionId,
                latitude,
                longitude,
                city,
                state,
                country);
    }

    @ApiMethod(name = "getProfileIdsByDistance")
    public ProfileIdList getProfileIdsByDistance(
            @Named("sessionId") long sessionId,
            @Named("delta") double delta) throws BadRequestException {

        return new ProfileIdList(GetProfilesByDistanceServerAction.getProfileIdsByDistance(
                sessionId,
                delta));
    }

    @ApiMethod(
            name = "getProfilesById",
            path = "getProfilesById",
            httpMethod = ApiMethod.HttpMethod.POST)
    public List<ProfileBean> getProfilesById(
            @Named("sessionId") long sessionId,
            @Named("profileIds")List<Long> profileIds) throws BadRequestException {

        return GetProfilesByIdServerAction.getProfilesById(sessionId, profileIds);
    }

    @ApiMethod(name = "reportRanking")
    public void reportRanking(
            @Named("sessionId") long sessionId,
            @Named("winnerId") long winnerId,
            @Named("loserId") long loserId) throws BadRequestException {

        ReportRankingServerAction.reportRanking(sessionId, winnerId, loserId);
    }

    @ApiMethod(name = "wipeTestData")
    public void wipeTestData(@Named("secret") long secret) throws BadRequestException {

        WipeTestDataServerAction.wipeTestData(secret);
    }

    @ApiMethod(name = "getMatches")
    public List<MatchBean> getMatches(@Named("sessionId") long sessionId)
            throws BadRequestException {

        return GetMatchesServerAction.getMatches(sessionId);
    }

    @ApiMethod(name = "sendMessage")
    public void sendMessage(
            @Named("sessionId") long sessionId,
            @Named("recipientId") long recipientId,
            @Named("message") String message) throws BadRequestException, IOException {

         SendMessageServerAction.sendMessage(sessionId, recipientId, message);
    }

    @ApiMethod(name = "getConversation")
    public List<MessageBean> getConversation(
            @Named("sessionId") long sessionId,
            @Named("otherUserId") long otherUserId) throws BadRequestException {

        return GetConversationServerAction.getConversation(sessionId, otherUserId);
    }

    @ApiMethod(name = "reportMessageRead")
    public void reportMessageRead(
            @Named("sessionId") long sessionId,
            @Named("otherUserId") long otherUserId,
            @Named("messageIndex") int messageIndex) throws BadRequestException {

        ReportMessageReadServerAction.reportMessageRead(sessionId, otherUserId, messageIndex);
    }

    @ApiMethod(name = "getMaxMessageIndex")
    public MaxMessageIndexResponseBean getMaxMessgeIndex(
            @Named("sessionId") long sessionId,
            @Named("partnerId") long partnerId) throws BadRequestException {

        int maxMessageIndex =
                GetMaxMessageIndexServerAction.getMaxMessageIndex(sessionId, partnerId);
        return new MaxMessageIndexResponseBean(maxMessageIndex);
    }

    @ApiMethod(name = "fuckOff")
    public void fuckOff(
            @Named("sessionId") long sessionId,
            @Named("partnerId") long partnerId) throws BadRequestException, IOException {

        FuckOffServerAction.fuckOff(sessionId, partnerId);
    }

    @ApiMethod(name = "reportAbuse")
    public void reportAbuse(
            @Named("sessionId") long sessionId,
            @Named("abuserId") long abuserId,
            @Named("note") String note) throws BadRequestException {

        ReportAbuseServerAction.reportAbuse(sessionId, abuserId, note);
    }

    @ApiMethod(name = "swapProfilePhotos")
    public UserBean swapProfilePhotos(
            @Named("sessionId") long sessionId,
            @Named("sourcePhotoId") int sourcePhotoId,
            @Named("destinationPhotoId") int destinationPhotoId) throws BadRequestException {

        return SwapProfilePhotosServerAction.swapProfilePhotos(
                sessionId,
                sourcePhotoId,
                destinationPhotoId);
    }

    @ApiMethod(name = "deactivateAccount")
    public UserBean deactivateAccount(
            @Named("sessionId") long sessionId,
            @Nullable @Named("reason") String reason)
            throws BadRequestException, IOException {

        return UpdateAccountStatusServerAction.deactivateAccount(sessionId, reason);
    }

    @ApiMethod(name = "reactivateAccount")
    public UserBean reactivateAccount(@Named("sessionId") long sessionId)
            throws BadRequestException, IOException {

        return UpdateAccountStatusServerAction.reactivateAccount(sessionId);
    }

    @ApiMethod(name = "getAdminStatistics")
    public AdminStatisticsBean getAdminStatistics(@Named("sessionId") long sessionId)
            throws BadRequestException {

        return GetAdminStatisticsServerAction.getAdminStatistics(sessionId);
    }

    @ApiMethod(name = "getAdminEgoEateruserDump")
    public List<AdminEgoEaterUserBean> getAdminEgoEateruserDump(@Named("sessionId") long sessionId)
            throws BadRequestException {

        return GetAdminDumpEgoEaterUserServerAction.getAdminEgoEateruserDump(sessionId);
    }

    @ApiMethod(name = "updateBirthdayOverride")
    public UserBean updateBirthdayOverride(
            @Named("sessionId") long sessionId,
            @Named("birthdayOverride") String birthdayOverride)
            throws BadRequestException, IOException {

        return UpdateBirthdayOverrideServerAction.updateBirthdayOverride(
                sessionId,
                birthdayOverride);
    }
}
