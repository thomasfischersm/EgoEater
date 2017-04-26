package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;

/**
 * A sub entity of {@link Conversation}. It represents a single message sent between two users.
 */
public class Message {

    private long index;
    private Ref<EgoEaterUser> senderProfileId;
    private long created;
    private boolean received;
    private String messageContent;

    public Message() {
    }

    public Message(long index, Ref<EgoEaterUser> senderProfileId, String messageContent) {
        this.index = index;
        this.senderProfileId = senderProfileId;
        this.messageContent = messageContent;

        created = System.currentTimeMillis();
        received = false;
    }

    public long getIndex() {
        return index;
    }

    public Ref<EgoEaterUser> getSenderProfileId() {
        return senderProfileId;
    }

    public long getCreated() {
        return created;
    }

    public boolean isReceived() {
        return received;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
