package com.playposse.egoeater.backend.serveractions.admin;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.beans.admin.AdminEgoEaterUserBean;
import com.playposse.egoeater.backend.schema.EgoEaterUser;
import com.playposse.egoeater.backend.serveractions.AbstractServerAction;

import java.util.ArrayList;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * A server action that downloads all the EgoEaterUser data for admins.
 */
public class GetAdminDumpEgoEaterUserServerAction extends AbstractServerAction {

    public static List<AdminEgoEaterUserBean> getAdminEgoEateruserDump(long sessionId)
            throws BadRequestException {

        // Ensure that only admins can execute this method.
        loadAdmin(sessionId);

        // Query data.
        List<EgoEaterUser> egoEaterUsers = ofy().load()
                .type(EgoEaterUser.class)
                .list();

        // Convert to result.
        List<AdminEgoEaterUserBean> result = new ArrayList<>(egoEaterUsers.size());
        for (EgoEaterUser egoEaterUser : egoEaterUsers) {
            result.add(new AdminEgoEaterUserBean(egoEaterUser));
        }

        return result;
    }
}
