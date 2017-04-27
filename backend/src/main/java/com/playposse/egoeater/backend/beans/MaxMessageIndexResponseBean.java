package com.playposse.egoeater.backend.beans;

/**
 * A transport bean that wraps an integer because the Google cloud endpoints API doesn't support
 * returning simply values.
 */
public class MaxMessageIndexResponseBean {

    private int maxMessageIndex;

    public MaxMessageIndexResponseBean(int maxMessageIndex) {
        this.maxMessageIndex = maxMessageIndex;
    }

    public int getMaxMessageIndex() {
        return maxMessageIndex;
    }
}
