package com.sr.voicechat;

public class RoomModel {
    private String roomOwnerId;
    private String roomName;
    private String roomBio;
    private String roomOwnerImage;

    public RoomModel() {}

    public RoomModel(String roomOwnerId, String roomName, String roomBio, String roomOwnerImage) {
        this.roomOwnerId = roomOwnerId;
        this.roomName = roomName;
        this.roomBio = roomBio;
        this.roomOwnerImage = roomOwnerImage;
    }

    public String getRoomOwnerId() { return roomOwnerId; }
    public String getRoomName() { return roomName; }
    public String getRoomBio() { return roomBio; }
    public String getRoomOwnerImage() { return roomOwnerImage; }
}
