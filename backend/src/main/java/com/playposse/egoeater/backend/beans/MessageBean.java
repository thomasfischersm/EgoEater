package com.playposse.egoeater.backend.beans;

import com.playposse.egoeater.backend.schema.Message;

/**
 * A transport bean that represents a single message in a conversation between two users.
 */
public class MessageBean {

    private long senderProfileId;
    private long messageIndex;
    private String messageContent;
    private boolean received;
    private long created;

    public MessageBean() {
    }

    public MessageBean(Message message) {
        senderProfileId = message.getSenderProfileId().getKey().getId();
        messageIndex = message.getIndex();
        messageContent = message.getMessageContent();
        received = message.isReceived();
        created = message.getCreated();
    }

    public long getSenderProfileId() {
        return senderProfileId;
    }

    public long getMessageIndex() {
        return messageIndex;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public boolean isReceived() {
        return received;
    }

    public long getCreated() {
        return created;
    }
}
