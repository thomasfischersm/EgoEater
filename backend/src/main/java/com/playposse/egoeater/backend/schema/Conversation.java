package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import java.util.ArrayList;
import java.util.List;

/**
 * An Objectify entity for the conversation between two users. A conversation consists of a list of
 * messages.
 * <p>
 * <p>The assumption is that a document based storage is better at loading one large document than
 * many small rows. Thus, the conversation is stored as one entity, rather than one entity for each
 * message.
 * <p>
 * <p>Profile A has always the lower profile id.
 */
@Entity
@Cache
public class Conversation {

    @Id private Long id;
    @Index private Ref<EgoEaterUser> profileRefA;
    @Index private Ref<EgoEaterUser> profileRefB;
    private Long created;
    private List<Message> messages;

    public Conversation() {
    }

    public Conversation(
            Ref<EgoEaterUser> profileRefA,
            Ref<EgoEaterUser> profileRefB,
            Ref<EgoEaterUser> initialSenderProfileRef,
            String initialMessage) {

        this.profileRefA = profileRefA;
        this.profileRefB = profileRefB;

        created = System.currentTimeMillis();
        messages = new ArrayList<>();

        // Create initial message.
        messages.add(new Message(0, initialSenderProfileRef, initialMessage));
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getProfileRefA() {
        return profileRefA;
    }

    public Ref<EgoEaterUser> getProfileRefB() {
        return profileRefB;
    }

    public Long getCreated() {
        return created;
    }

    public List<Message> getMessages() {
        return messages;
    }
}
