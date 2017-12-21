package com.playposse.egoeater.backend.beans.admin;

import com.playposse.egoeater.backend.schema.Conversation;
import com.playposse.egoeater.backend.schema.Message;

import java.util.List;

/**
 * A transport bean that exports the messages.
 */
public class AdminMessageBean {

    private long conversationId;
    private long senderProfileId;
    private long recipientProfileId;
    private long messageIndex;
    private String messageContent;
    private boolean received;
    private long created;

    public AdminMessageBean() {
    }

    public AdminMessageBean(
            long conversationId,
            long senderProfileId,
            long recipientProfileId,
            long messageIndex,
            String messageContent,
            boolean received,
            long created) {

        this.conversationId = conversationId;
        this.senderProfileId = senderProfileId;
        this.recipientProfileId = recipientProfileId;
        this.messageIndex = messageIndex;
        this.messageContent = messageContent;
        this.received = received;
        this.created = created;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getSenderProfileId() {
        return senderProfileId;
    }

    public void setSenderProfileId(long senderProfileId) {
        this.senderProfileId = senderProfileId;
    }

    public long getRecipientProfileId() {
        return recipientProfileId;
    }

    public void setRecipientProfileId(long recipientProfileId) {
        this.recipientProfileId = recipientProfileId;
    }

    public long getMessageIndex() {
        return messageIndex;
    }

    public void setMessageIndex(long messageIndex) {
        this.messageIndex = messageIndex;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public static void convertAndAdd(
            List<AdminMessageBean> messageBeans,
            Conversation conversation) {

        if (conversation.getMessages() != null) {
            long profileIdA = conversation.getProfileRefA().getKey().getId();
            long profileIdB = conversation.getProfileRefB().getKey().getId();

            for (Message message : conversation.getMessages()) {
                long senderId = message.getSenderProfileId().getKey().getId();
                long recipientId = (profileIdA == senderId) ? profileIdB : profileIdA;

                messageBeans.add(new AdminMessageBean(
                        conversation.getId(),
                        senderId,
                        recipientId,
                        message.getIndex(),
                        message.getMessageContent(),
                        message.isReceived(),
                        message.getCreated()));
            }
        }
    }
}
