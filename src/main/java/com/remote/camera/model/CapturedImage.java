package com.remote.camera.model;

public class CapturedImage {
    private String base64Data;
    private String timestamp;

    public CapturedImage(String base64Data, String timestamp) {
        this.base64Data = base64Data;
        this.timestamp = timestamp;
    }

    public String getBase64Data() { return base64Data; }
    public String getTimestamp() { return timestamp; }    
}
