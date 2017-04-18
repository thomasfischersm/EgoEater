package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.playposse.egoeater.backend.generatematches.GenerateMatchesServlet;

import javax.annotation.Nullable;

/**
 * An Objectify entity that records each time the {@link GenerateMatchesServlet} runs.
 */
@Entity
public class MatchesServletLog {

    @Id
    private Long id;
    private long date = System.currentTimeMillis();
    private int duration;
    private int status;
    @Nullable
    private String errorMessage;
    private boolean isRunByCron;

    public MatchesServletLog(
            int duration,
            int status,
            boolean isRunByCron,
            @Nullable String errorMessage) {

        this.duration = duration;
        this.status = status;
        this.isRunByCron = isRunByCron;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public long getDate() {
        return date;
    }

    public int getDuration() {
        return duration;
    }

    public int getStatus() {
        return status;
    }

    public boolean isRunByCron() {
        return isRunByCron;
    }
}
