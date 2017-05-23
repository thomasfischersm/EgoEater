package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * An Objectify entity that contains the report of an abuse.
 */
@Entity
public class AbuseReport {

    @Id
    private Long id;
    @Index
    private Ref<EgoEaterUser> reporterRef;
    @Index
    private Ref<EgoEaterUser> abuserRef;
    @Index
    private Long created;
    private String note;

    public AbuseReport() {
    }

    public AbuseReport(Ref<EgoEaterUser> reporterRef, Ref<EgoEaterUser> abuserRef, String note) {
        this.reporterRef = reporterRef;
        this.abuserRef = abuserRef;
        this.note = note;

        created = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Ref<EgoEaterUser> getReporterRef() {
        return reporterRef;
    }

    public Ref<EgoEaterUser> getAbuserRef() {
        return abuserRef;
    }

    public Long getCreated() {
        return created;
    }

    public String getNote() {
        return note;
    }
}
