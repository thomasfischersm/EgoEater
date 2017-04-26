package com.playposse.egoeater.firebase;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Base message class for Firebase messages. Firebase sends the data in messages simply as
 * a map. Implementations of this class will strongly type the data.
 */
public abstract class FirebaseMessage {

    protected final Map<String, String> data;

    public FirebaseMessage(RemoteMessage message) {
        data = message.getData();
    }

    protected Long getLong(String key) {
        return Long.valueOf(data.get(key));
    }

    protected Boolean getBoolean(String key) {
        return Boolean.valueOf(data.get(key));
    }

    protected Integer getInteger(String key) {
        return Integer.valueOf(data.get(key));
    }
}
