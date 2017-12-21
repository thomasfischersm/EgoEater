package com.playposse.egoeater.backend.serveractions.admin;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.admin.AdminMessageBean;
import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.serveractions.AbstractServerAction;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that's only accessible to admins. It dumps all the messages out for customer
 * service and analysis purposes.
 */
public class GetAdminDumpMessageServerAction extends AbstractServerAction {

    public static List<AdminMessageBean> getAdminMessageDump(long sessionId) throws BadRequestException {

        // Ensure that only admins can execute this method.
        loadAdmin(sessionId);

        // Query data.
        List<Conversation> conversations = ofy().load()
                .type(Conversation.class)
                .list();

        // Convert to transport beans.
        List<AdminMessageBean> messageBeans = new ArrayList<>();
        for (Conversation conversation : conversations) {
            AdminMessageBean.convertAndAdd(messageBeans, conversation);
        }

        return messageBeans;
    }
}
