package com.sr.voicechat;

public class RoomModel {
    public String roomId;
    public String roomName;
    public String roomBio;
    public String roomImage;
    public String ownerId;
    public long createdAt;

    public RoomModel() {}

    public RoomModel(String roomId, String roomName, String roomBio, String roomImage, String ownerId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomBio = roomBio;
        this.roomImage = roomImage;
        this.ownerId = ownerId;
        this.createdAt = System.currentTimeMillis();
    }
}
