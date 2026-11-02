package com.sr.voicechat;

public class RoomModel {
    private String roomId;
    private String roomName;
    private String roomImage;
    private int onlineCount;
    private String topUserName;

    public RoomModel() {}

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getOnlineCount() { return onlineCount; }
    public void setOnlineCount(int onlineCount) { this.onlineCount = onlineCount; }

    public String getRoomImage() { return roomImage; }
    public void setRoomImage(String roomImage) { this.roomImage = roomImage; }

    public String getTopUserName() { return topUserName; }
    public void setTopUserName(String topUserName) { this.topUserName = topUserName; }
}
