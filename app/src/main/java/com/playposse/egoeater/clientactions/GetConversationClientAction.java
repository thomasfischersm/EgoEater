package com.playposse.egoeater.clientactions;

import android.content.Context;

import com.playposse.egoeater.backend.egoEaterApi.model.MessageBean;
import com.playposse.egoeater.backend.egoEaterApi.model.MessageBeanCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A client action that retrieves the entire conversation between two users.
 */
public class GetConversationClientAction extends ApiClientAction<List<MessageBean>> {

    private final long profileId;

    public GetConversationClientAction(Context context, long profileId) {
        super(context);

        this.profileId = profileId;
    }

    @Override
    protected List<MessageBean> executeAsync() throws IOException {
        MessageBeanCollection collection =
                getApi().getConversation(getSessionId(), profileId).execute();

        if ((collection == null) || (collection.getItems() == null)) {
            return new ArrayList<>();
        } else {
            return collection.getItems();
        }
    }

    public static List<MessageBean> getBlocking(Context context, long profileId)
            throws InterruptedException {

        GetConversationClientAction action = new GetConversationClientAction(context, profileId);
        return action.executeBlocking();
    }
}
