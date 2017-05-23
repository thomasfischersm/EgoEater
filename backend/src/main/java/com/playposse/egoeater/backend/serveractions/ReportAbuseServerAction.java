package com.playposse.egoeater.backend.serveractions;

import com.google.api.server.spi.response.BadRequestException;
import com.googlecode.objectify.Ref;
import com.playposse.egoeater.backend.schema.AbuseReport;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.util.RefUtil;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that lets a user report the abuse of service by another user.
 */
public class ReportAbuseServerAction extends AbstractServerAction {

    private static final Logger log = Logger.getLogger(ReportAbuseServerAction.class.getName());

    public static void reportAbuse(long sessionId, long abuserId, String note)
            throws BadRequestException {

        // Verify session id and find user.
        EgoEaterUser egoEaterUser = loadUser(sessionId);
        Ref<EgoEaterUser> userRef = RefUtil.createUserRef(egoEaterUser);

        // Create AbuseReport entry.
        Ref<EgoEaterUser> abuserRef = RefUtil.createUserRef(abuserId);
        AbuseReport abuseReport = new AbuseReport(userRef, abuserRef, note);
        ofy().save().entity(abuseReport);

        // Send an e-mail to the admin
        sendEmailToAdmin(note);
    }

    private static void sendEmailToAdmin(String note) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress("thomaspaniolo@gmail.com", "Thomas Fischer"));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress("thomaspaniolo@gmail.com", "Thomas Fischer"));
            msg.setSubject("An Ego Eater User Reported An Abuse");
            String cleanNote = note.replaceAll("<", ""); // Prevent simple attacks.
            msg.setText("Details: " + cleanNote);
            Transport.send(msg);
        } catch (MessagingException | UnsupportedEncodingException ex) {
            log.log(Level.SEVERE, "Failed to e-mail an abuse notification. ", ex);
        }
    }
}
