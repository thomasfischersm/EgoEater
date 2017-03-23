package com.playposse.egoeater.backend.schema;

import com.googlecode.objectify.annotation.Index;

/**
 * An Objectify entity that represents a profile photo. The actual photo is stored in Google Cloud
 * Storage.
 */
public class ProfilePhoto {

    @Index private Long id;
    private String fileName;
    private String url;
    private long created;

    public ProfilePhoto() {
    }

    public ProfilePhoto(String fileName, String url) {
        this.fileName = fileName;
        this.url = url;

        created = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public long getCreated() {
        return created;
    }
}
