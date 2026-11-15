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

    // NEW - Home থেকে নতুন ছবি বসানোর জন্য, আগেরটা ঠিকই থাকবে
    public String getRoomImage() { return roomOwnerImage; }
    public void setRoomImage(String img) { this.roomOwnerImage = img; }
    public void setRoomOwnerImage(String img) { this.roomOwnerImage = img; }
    public void setRoomOwnerId(String id) { this.roomOwnerId = id; }
    public void setRoomName(String n) { this.roomName = n; }
    public void setRoomBio(String b) { this.roomBio = b; }
}
