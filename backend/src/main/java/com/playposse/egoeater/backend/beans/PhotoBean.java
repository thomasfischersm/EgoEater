package com.playposse.egoeater.backend.beans;

/**
 * A transport bean that carries the bytes of an image file. The file is base64 encoded and expected
 * to be a PNG.
 */

public class PhotoBean {
    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
